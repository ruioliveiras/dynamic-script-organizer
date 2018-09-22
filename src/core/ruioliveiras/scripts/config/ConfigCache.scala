package ruioliveiras.scripts.config

import java.io.File

import boopickle.DefaultBasic._
import boopickle.PickleState
import ruioliveiras.scripts.model._

/**
  * Created by ruioliveira at 2/12/18
  */
class ConfigCache {
  implicit val commandPickler: Pickler[Command] = PicklerGenerator.generatePickler[Command]
  implicit val infraPickler: Pickler[ScriptsConfig] = PicklerGenerator.generatePickler[ScriptsConfig]
  implicit val configPicler: Pickler[ConfigElement] = PicklerGenerator.generatePickler[ConfigElement]


  def store(configFile: String, store: ScriptsConfig) = {
    val write = Pickle.intoBytes(store)(implicitly[PickleState], infraPickler)

    import java.io.FileOutputStream
    val out = new FileOutputStream(configFile + "/cache.conf.tmp")
    out.getChannel.write(write)
    out.close()
  }

  def read(configFile: String): Option[ScriptsConfig] = {
    if (!new File(configFile + "/cache.conf.tmp").exists()) return Option.empty[ScriptsConfig]
    import java.io.FileInputStream
    import java.nio.ByteBuffer
    val buf = ByteBuffer.allocate(1048 * 500)
    val in = new FileInputStream(configFile + "/cache.conf.tmp")
    val len = in.getChannel.read(buf)
    in.close()

    Option(Unpickle[ScriptsConfig](infraPickler).fromBytes(ByteBuffer.wrap(buf.array())))
  }
}
