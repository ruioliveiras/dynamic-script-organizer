package config

import java.io.File

import com.typesafe.config.ConfigFactory
import model.Infra

/**
  * Created by ruioliveira at 2/12/18
  */
class ConfigProvider {
  protected val cache:ConfigCache = new ConfigCache()
  protected val reader:ConfigReader = new ConfigReader()

  def get(configFile:String):Infra = {
    cache.read(configFile).getOrElse(this.regenerate(configFile))
  }

  def regenerate(configFile:String): Infra = {
    val config = ConfigFactory.parseFile(new File(configFile)).resolve()
    val infra = reader.read(configFile, config)
    cache.store(configFile, infra)
    infra
  }

}
