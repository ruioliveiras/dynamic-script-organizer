package ruioliveiras.scripts

import java.io.{Reader, StringReader, Writer}
import java.util

import com.github.mustachejava.{DefaultMustacheFactory, MustacheResolver}
import ruioliveiras.scripts.model.{ConfigElement, Execution}

/**
  * Created by ruioliveira at 2/12/18
  */
class Mustache {

  def render(template:String, data:Execution)(implicit writer: Writer) = {
    val cleanData = mapData(data)
    val mf = new DefaultMustacheFactory(new MustacheResolver {
      override def getReader(resourceName: String): Reader = {
        val tokens = resourceName.split("\\.")
        new StringReader(recursiveGet(tokens, cleanData))
      }
    })
    val mustache = mf.compile(new StringReader(template), "n")

    mustache.execute(writer, cleanData).flush()
  }

  def mapData(data:Execution):util.Map[String, AnyRef] = {
    // make sure that List("a"  -> 1, "a" -> 2).toMap is Map("a" -> 2)
    lazy val map:Map[String, Any] = data.varOrder.flatMap(data.mustacheData.apply(_).extras).toMap
    val argsData = data.args.zipWithIndex.map{case (arg, i) => s"arg$i" -> arg}.toMap
    val d = data.mustacheData ++ argsData ++ Map("args" -> data.args.map(x => s"'$x'").mkString(" "))
    new SimpleMap(d) {
      override def get(key: scala.Any): AnyRef = recursiveToJava(data.mustacheData.get(key.toString).orElse(map.get(key.toString)))
    }
  }

  def recursiveGet(tokens:Array[String], cleanData:util.Map[String, AnyRef]):String = {
    var i = 0
    while(i < tokens.length) {
      val k = cleanData.get(tokens(i))
      if (k == null) {
        throw new RuntimeException(s"Not found ${tokens.mkString(".")}, '${tokens(i)}'")
      } else if (k.isInstanceOf[String]) {
        return k.asInstanceOf[String]
      }else if (! k.isInstanceOf[util.Map[String, AnyRef]]) {
        throw new RuntimeException(s"Not a Map or String ${tokens.mkString(".")}, '${tokens(i)}' ${k.getClass.getSimpleName}")
      }
      i += 1
    }
    throw new RuntimeException(s"Template not found ${tokens.mkString(".")}")
  }

  def recursiveToJava(data:Any):AnyRef = data match {
    case None => null
    case Some(x) => recursiveToJava(x)
    case i:Iterable[Any] =>
      val x = new util.ArrayList[AnyRef](i.size)
      i.foreach(e => x.add(recursiveToJava(e)))
      x
    case x:Map[String, Any] =>
      val m:java.util.Map[Any, Any] = new java.util.HashMap[Any, Any]()
      x.foreach{case (k, v) => m.put(recursiveToJava(k), recursiveToJava(v))}
      m
    case x:ConfigElement =>
      new SimpleMap(x.extras ++ Map("name" -> x.name)) {
        override def get(key: scala.Any): AnyRef = {
          recursiveToJava(x.extras.get(key.toString).orElse(x.fields.get(key.toString).map(_.values)))
        }
      }
    case x:String => x
    case x:Int => new Integer(x)
  }


  abstract class SimpleMap(m:Map[String, Any]) extends util.Map[String, AnyRef]{
    override def isEmpty: Boolean = m.isEmpty
    override def size(): Int = m.size
    override def containsKey(key: scala.AnyRef): Boolean = key match {
      case x:String => m.contains(x)
      case _ => false
    }
    override def values(): util.Collection[AnyRef] = {
      val arrayList = new util.ArrayList[AnyRef](m.size)
      m.foreach(e => arrayList.add(recursiveToJava(e._2)))
      arrayList
    }
    // NOT IMPLEMENTED
    override def keySet(): util.Set[String] = ???
    override def entrySet(): util.Set[util.Map.Entry[String, AnyRef]] = ???
    override def containsValue(value: scala.AnyRef): Boolean = ???
    override def remove(key: scala.AnyRef): AnyRef = ???
    override def put(key: String, value: AnyRef): AnyRef = ???
    override def putAll(m: util.Map[_ <: String, _ <: AnyRef]): Unit = ???
    override def clear(): Unit = ???
  }
}
