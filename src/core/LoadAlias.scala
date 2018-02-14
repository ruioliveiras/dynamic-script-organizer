import java.io.StringWriter

import model.{Command, ConfigElement}

/**
  * Created by ruioliveira at 2/12/18
  */
class LoadAlias()(implicit main:Application) {

  def writeAlias(configFile:String, infra:model.Infra) = {

    import java.io.{BufferedWriter, FileWriter}

    val writer: BufferedWriter = new BufferedWriter(new FileWriter(infra.sourceFile))
    writer.write(s"alias ${infra.name}='java -Dconfig=$configFile -jar ${infra.jarFile} '\n")
    writer.write(s"alias ${infra.name}.reload='source $$(java -Dconfig=$configFile -jar ${infra.jarFile} -r)'\n")


    def printCommad(path:String, c:Command) = {
      val cmd = c.name

      val aliasCommand:String = c.typ match {
        case "alias" =>
          implicit val writer = new StringWriter()
          main.exec(s"$path$cmd", List())
          writer.getBuffer.toString
        case _ =>
          s"java -Dconfig=$configFile -jar ${infra.jarFile} $path$cmd"
      }
      writer.write(s"alias ${infra.name}.$path$cmd='$aliasCommand'\n")
    }

    def printConfig(path:String, c:ConfigElement):Unit =  {
      c.cmds.foreach(cmd => printCommad(path, infra.commands(cmd)))
      c.edges.mapValues(_._2).foreach{case (k, subc) => printConfig(s"$path$k.", subc) }
    }
    printConfig("", infra.config)
    writer.flush()
    writer.close()

    println(infra.sourceFile)
  }
}
