import java.io.{Reader, StringReader, Writer}
import java.util

import com.github.mustachejava.{DefaultMustacheFactory, MustacheResolver}

/**
  * Created by ruioliveira at 2/12/18
  */
class Mustache {

  def render(template:String, data:Any)(implicit writer: Writer) = {
    val cleanData = recursiveToJava(data)
    val mf = new DefaultMustacheFactory(new MustacheResolver {
      override def getReader(resourceName: String): Reader = {
        val Array(a,b) = resourceName.split("\\.")
        new StringReader(cleanData.asInstanceOf[util.HashMap[String, Any]].get(a).asInstanceOf[util.HashMap[String, Any]].get(b).asInstanceOf[String])
      }
    })
    val mustache = mf.compile(new StringReader(template), "n")

    mustache.execute(writer, cleanData).flush()
  }


  def recursiveToJava(data:Any):Any = data match {
    case x:Map[Any, Any] =>
      val m:java.util.Map[Any, Any] = new java.util.HashMap[Any, Any]()
      x.foreach{case (k, v) => m.put(recursiveToJava(k), recursiveToJava(v))}
      m

    case x => x
  }
}
