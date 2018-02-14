package config

import java.io.File

import boopickle.DefaultBasic._
import boopickle.PickleState
import model._

/**
  * Created by ruioliveira at 2/12/18
  */
class ConfigCache {
  implicit val commandPickler: Pickler[Command] = PicklerGenerator.generatePickler[Command]
  implicit val infraPickler: Pickler[Infra] = PicklerGenerator.generatePickler[Infra]
  implicit val configPicler: Pickler[ConfigElement] = PicklerGenerator.generatePickler[ConfigElement]


  def store(configFile:String, store:Infra) = {
    val write = Pickle.intoBytes(store)(implicitly[PickleState], infraPickler)

    import java.io.FileOutputStream
    val out = new FileOutputStream(configFile + ".tmp")
    out.getChannel.write(write)
    out.close()
  }

  def read(configFile:String):Option[Infra] = {
    if (! new File(configFile + ".tmp").exists()) return Option.empty[Infra]
    import java.io.FileInputStream
    import java.nio.ByteBuffer
    val buf = ByteBuffer.allocate(1048 * 500)
    val in = new FileInputStream(configFile + ".tmp")
    val len = in.getChannel.read(buf)
    in.close()

    Option(Unpickle[Infra](infraPickler).fromBytes(ByteBuffer.wrap(buf.array())))
  }
}
