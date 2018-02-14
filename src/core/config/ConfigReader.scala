package config

import com.typesafe.config.{Config, ConfigException}
import model.{Command, ConfigElement, Infra}

import scala.collection.JavaConversions._


/**
  * Created by ruioliveira at 2/12/18
  */
class ConfigReader {
  def read(c:Config):Infra = {
    val name = c.getString("name")
    val sourceFile =c.getString("sourceFile")

    val configElem = this.readConfigElement(c.getConfig("struct"))
    val commands = this.readCommands(c.getConfig("commands"))
    val i = Infra(name, sourceFile, commands , configElem)
    i
  }

  private def readConfigElement(c:Config):ConfigElement = {
    val cmds = c.getStringList("cmds").toList
    val extras = this.readExtras(c)
    val fieldKeys = this.readKeys(c)(Set("cmds"))

    //println(extras)
    /*
    println(fieldKeys.map(k => k -> k).toMap.mapValues(c.getConfig).mapValues(x => (x, readKeys(x)(Set())))
      .mapValues{case (config, keys) =>
        keys.map(key =>key -> config.getConfig(key)).toMap.mapValues(readConfigElement)
      }
    )
    */

    val fields:Map[String, Map[String, ConfigElement]] = fieldKeys.map(k => k -> k).toMap
      .mapValues(c.getConfig)
      .mapValues(x => (x, this.readKeys(x)(Set())))
      .mapValues{case (config, keys) =>
        keys.map(key =>key -> config.getConfig(key)).toMap.mapValues(readConfigElement)
      }
    //println(fields)

    ConfigElement(fields, extras, cmds.toSet)
  }

  private def readCommands(config: Config):Map[String, Command] = {
    val commandKeys = this.readKeys(config)(Set())

    commandKeys.map( k => k -> k).toMap
      .mapValues(config.getConfig)
      .map{case (k, config) =>
        val typ = config.getString("type")
        val code = config.getString("code")
        k -> Command(k, typ, code)
      }
  }

  private  def readKeys(config:Config)(exclude:Set[String]):List[String] = {
    val elemetKeys = config.root().toList.map(_._1)
      .filter(!exclude.contains(_))
      .filter(!_.startsWith("_"))
    elemetKeys
  }

  private  def readExtras(config:Config):Map[String, String] = {
    val keys = config.root().toList.map(_._1).filter(_.startsWith("_"))

    keys.filter{key =>
      try{
        config.getString(key)
        true
      }catch {
        case e: ConfigException.WrongType =>
          e.printStackTrace(System.err)
          false
      }
    }.map{ key =>
      val value = config.getString(key)
      key.substring(1) -> value
    }.toMap
  }
}
