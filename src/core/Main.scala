import java.io.{PrintWriter, Writer}

import config.ConfigProvider
import model.Infra


/**
  * Created by ruioliveira at 1/18/18
  */
object Main{
  def main(args:Array[String]):Unit = {
    val config = System.getProperty("config")
    val cmd = args.apply(0)
    val main = new Application(config)

    if (cmd == "-r") {
      main.initRegenerate()
      return
    }
    main.init()
    main.exec(cmd, args.tail.toList)(new PrintWriter(System.out))
  }
}

class Application(configFile:String){
  implicit val main = this
  val configProvider = new ConfigProvider()
  val mustache = new Mustache()
  val loadAlias = new LoadAlias()
  var infra:Infra = null


  def init() = {
    infra = configProvider.get(configFile)
  }

  def initRegenerate(): Unit ={
    infra = configProvider.regenerate(configFile)
  }

  def exec(token:String, args:List[String])(implicit resultWriter:Writer) = {
    val argsData = args.zipWithIndex.map{case (arg, i) => s"arg$i" -> arg}.toMap
    val execution = infra.execute(token)
    val command = infra.commands(execution.cmd)
    // render command
    mustache.render(command.code, execution.mustacheData ++ argsData ++ Map("args" -> args.map(x => s"'$x'").mkString(" ")))
  }
}
