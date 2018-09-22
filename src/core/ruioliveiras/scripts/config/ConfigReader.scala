package ruioliveiras.scripts.config

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import ruioliveiras.scripts.model.{Command, ConfigElement, ScriptsConfig}

import scala.collection.JavaConversions._


/**
  * Created by ruioliveira at 2/12/18
  */
class ConfigReader {
  def read(configFile: String, c: Config): ScriptsConfig = {
    val name = configFile.split("/").last

    requires("In root", c)("struct", "commands")
    val configElem = this.readConfigElement(c.getConfig("struct"), "root")
    val commands = this.readCommands(c.getConfig("commands"))
    val i = ScriptsConfig(name, configFile , commands, configElem)
    i
  }

  private def readConfigElement(c1: Config, name: String): ConfigElement = {
    val c = c1.withFallback(ConfigFactory.parseString("""{ cmds=[] , defaultCmd = "" }"""))
    requires(s"In command $name", c)("cmds")
    val cmds = c.getStringList("cmds").toList
    val defaultCmd = c.getString("defaultCmd")
    val extras = this.readExtras(c)
    val fieldKeys = this.readKeys(c)(Set("cmds", "defaultCmd"))

    //println(extras)
    /*
    println(fieldKeys.map(k => k -> k).toMap.mapValues(c.getConfig).mapValues(x => (x, readKeys(x)(Set())))
      .mapValues{case (ruioliveiras.scripts.config, keys) =>
        keys.map(key =>key -> ruioliveiras.scripts.config.getConfig(key)).toMap.mapValues(readConfigElement)
      }
    )
    */

    val fields: Map[String, Map[String, ConfigElement]] = fieldKeys.map(k => k -> k).toMap
      .mapValues(c.getConfig)
      .mapValues(x => (x, this.readKeys(x)(Set())))
      .mapValues { case (config, keys) =>
        keys.map(key => key -> readConfigElement(config.getConfig(key), key)).toMap
      }
    //println(fields)

    ConfigElement(fields, extras, cmds.toSet, name, Some(defaultCmd).filter(_.nonEmpty))
  }

  private def readCommands(config: Config): Map[String, Command] = {
    val commandKeys = this.readKeys(config)(Set())



    commandKeys.map(k => k -> k).toMap
      .mapValues(config.getConfig)
      .mapValues(config => config.withFallback(ConfigFactory.parseString("""help=""""")))
      .map { case (k, config) =>
        requires(s"In command $k", config)("type", "code", "help")
        val typ = config.getString("type")
        val code = config.getString("code")
        val help = config.getString("help")
        k -> Command(k, help, typ, code)
      }
  }

  private def readKeys(config: Config)(exclude: Set[String]): List[String] = {
    val elemetKeys = config.root().toList.map(_._1)
      .filter(!exclude.contains(_))
      .filter(!_.startsWith("_"))
    elemetKeys
  }

  private def readExtras(config: Config): Map[String, String] = {
    val keys = config.root().toList.map(_._1).filter(_.startsWith("_"))

    keys.filter { key =>
      try {
        config.getString(key)
        true
      } catch {
        case e: ConfigException.WrongType =>
          e.printStackTrace(System.err)
          false
      }
    }.map { key =>
      val value = config.getString(key)
      key.substring(1) -> value
    }.toMap
  }

  private def requires(msg:String, c:Config)(fields:String*) = {
    fields.foreach{ field =>
      if (! c.hasPath(field)) {
        throw new RuntimeException(s"$msg Missing fields '$field'")
      }
    }
  }
}
