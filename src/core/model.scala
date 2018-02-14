


package object model {

  case class Command(name:String, typ:String, code:String)
  case class Infra(name:String, sourceFile:String, commands:Map[String, Command], config:ConfigElement) {
    def execute(cmd:String) = config.execute(cmd)
    def jarFile:String = System.getenv("INFRA_HOME") + "/infra.jar"
  }
  case class ConfigElement(fields:Map[String, Map[String, ConfigElement]], extras:Map[String, String], cmds:Set[String]) {
    lazy val edges:Map[String, (String, ConfigElement)] = fields.flatMap { case (varName, map) => map.map { case (code, config) => (code, (varName, config)) }.toList }

    def execute(cmd:String):Execution = {
      this.readPath(cmd.split("\\."), 0)
    }
    protected def readPath(token:Array[String], i:Int):Execution = {
      if (token.size - 1 == i) {
        return Execution(token(i), Map())
      }

      val (fieldName, configElem) = edges(token(i))
      val exec = configElem.readPath(token, i + 1)
      exec.withData(Map(fieldName -> configElem.extras))
    }
  }


  case class Execution(cmd:String, mustacheData:Map[String /*varname*/, Map[String, String] /*extas*/ ]) {
    def withData(data:Map[String, Map[String, String]]) = this.copy(mustacheData = this.mustacheData ++ data)
  }




}
