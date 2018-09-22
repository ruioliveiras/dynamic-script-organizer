package ruioliveiras.scripts

import java.io.{PrintWriter, Writer}

import ruioliveiras.scripts.config.ConfigProvider
import ruioliveiras.scripts.model.ScriptsConfig


/**
  * Created by ruioliveira at 1/18/18
  */
object Main{
  def main(args:Array[String]):Unit = {
    val config = System.getProperty("config")
    val cmd = args.apply(0)
    val cmdFlag = if (args.size > 1) {
      Option(args.apply(1))
    } else {
      None
    }
    val main = new Application(config)

    if (cmd.startsWith("-")) {
      if (cmd == "-r") {
        main.initRegenerate()
        return
      }else if (cmd == "-d") {
        main.init()
        main.debug()
        return
      }
    }
    if (cmdFlag.isDefined && cmdFlag.get == "-h") {
      main.init()
      main.help(cmd)
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
  val scriptsBuilders = new BuildScripts()
  var infra:ScriptsConfig = null


  def init() = {
    infra = configProvider.get(configFile)
  }

  def initRegenerate(): Unit ={
    infra = configProvider.regenerate(configFile)
    scriptsBuilders.builScripts(configFile, infra)
  }

  def help(cmd:String):Unit = {
    val execution = infra.execute(cmd)
    val command = infra.commands(execution.cmd)

    println(command.help)
  }

  def debug(): Unit = {

  }

  def exec(token:String, args:List[String])(implicit resultWriter:Writer) = {
    val execution = infra.execute(token)
    val command = infra.commands(execution.cmd)
    // .mustacheData ++ argsData ++ Map("args" -> args.map(x => s"'$x'").mkString(" ")
    execution.withArguments(args)
    // render command
    mustache.render(command.code, execution)
  }
}
