package ruioliveiras.scripts

package object model {

  case class Command(name:String, help:String, typ:String, code:String)
  case class ScriptsConfig(name:String, baseFile:String, commands:Map[String, Command], config:ConfigElement) {
    def execute(cmd:String) = config.execute(cmd)
    def jarFile:String = System.getenv("INFRA_HOME") + "/infra.jar"
    def infraBash:String = System.getenv("INFRA_HOME") + "/infra-bash"
    def sourceFile:String = baseFile + "/setup.source"
    def cmdPath:String = baseFile + "/path"
  }

  case class ConfigElement(fields:Map[String/*Fields*/, Map[String /*Code*/, ConfigElement]], extras:Map[String, String], cmds:Set[String], name:String, defaultCmd:Option[String]) {
    lazy val edges:Map[String, (String, ConfigElement)] = fields.flatMap { case (varName, map) => map.map { case (code, config) => (code, (varName, config)) }.toList }

    def execute(cmd:String):Execution = {
      this.readPath(cmd.split("\\."), 0)
    }
    protected def readPath(token:Array[String], i:Int):Execution = {
      def notFound = new RuntimeException(s"Command not found ${token.mkString(".")}")
      if (i >= token.length  ) {
        throw notFound
      }

      val tokenv = token(i)
      if (edges.contains(tokenv)) {
        val (fieldName, configElem) = edges(token(i))
        val exec = if (configElem.defaultCmd.nonEmpty && token.length >= i + 1) {
          Execution(configElem.defaultCmd.get, Map(), List())
        } else {
          configElem.readPath(token, i + 1)
        }
        exec.addData(fieldName, configElem)
      } else if (cmds.contains(tokenv)) {
        Execution(token(i), Map(), List())
      } else {
        throw notFound
      }
    }
  }


  case class Execution(cmd:String, mustacheData:Map[String /*varname*/, ConfigElement], varOrder:List[String], args:List[String] = List()) {
    def addData(varName:String, elem:ConfigElement) =
      this.copy(mustacheData = this.mustacheData.+(varName -> elem), varOrder = this.varOrder :+ varName)

    def withArguments(args:List[String]) = {
      this.copy(args = args)
    }
    //def withData(data:Map[String, Map[String, String]]) = this.copy(mustacheData = this.mustacheData ++ data)
  }




}
