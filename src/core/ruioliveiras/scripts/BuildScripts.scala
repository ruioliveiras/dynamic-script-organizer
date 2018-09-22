package ruioliveiras.scripts

import java.io.StringWriter

import ruioliveiras.scripts.model.{Command, ConfigElement}

/**
  * Created by ruioliveira at 2/12/18
  */
class BuildScripts()(implicit main: Application) {

  def builScripts(configFile: String, infra: model.ScriptsConfig) = {

    import java.io.{BufferedWriter, FileWriter}

    val writer: BufferedWriter = new BufferedWriter(new FileWriter(infra.sourceFile))
    writer.write(s"export PATH=${infra.cmdPath}:$$PATH\n")
    writer.write(s"alias ${infra.name}='java -Dconfig=$configFile -jar ${infra.jarFile} '\n")
    writer.write(s"alias ${infra.name}.reload='rm ${infra.cmdPath}/* ; source $$(java -Dconfig=$configFile -jar ${infra.jarFile} -r)'\n")


    def printCommad(path: String, c: Command) = {
      def withAlias(s:String ) = s"alias ${infra.name}.$path='$s'\n"

      val commands: String = c.typ match {
        case "alias" =>
          val resultWriter = new StringWriter()
          main.exec(s"$path", List())(resultWriter)
          withAlias(resultWriter.getBuffer.toString)
        case "executor" =>
          withAlias(s"${infra.infraBash} '$configFile' $path")
        case "bash" =>
          val fileName = infra.cmdPath + s"/${infra.name}.$path"
          val resultWriter: BufferedWriter = new BufferedWriter(new FileWriter(fileName))
          resultWriter.write("#!/bin/bash\n")
          main.exec(s"$path", List())(resultWriter)
          s"chmod +x $fileName \n"
        case _ =>
          withAlias(s"java -Dconfig=$configFile -jar ${infra.jarFile} $path")
      }

      writer.write(commands)
    }

    def printConfig(path: String, c: ConfigElement): Unit = {
      c.cmds.foreach(cmd => printCommad(s"$path$cmd", infra.commands(cmd)))
      c.defaultCmd.foreach(cmd => printCommad(s"${path.init}", infra.commands(cmd)))
      c.edges.mapValues(_._2).foreach { case (k, subc) => printConfig(s"$path$k.", subc) }
    }

    printConfig("", infra.config)
    writer.flush()
    writer.close()

    println(infra.sourceFile)
  }
}
