


package object model {

  case class Command(name:String, help:String, typ:String, code:String)
  case class Infra(name:String, sourceFile:String, commands:Map[String, Command], config:ConfigElement) {
    def execute(cmd:String) = config.execute(cmd)
    def jarFile:String = System.getenv("INFRA_HOME") + "/infra.jar"
    def infraBash:String = System.getenv("INFRA_HOME") + "/infra-bash"
  }
  case class ConfigElement(fields:Map[String/*Fields*/, Map[String /*Code*/, ConfigElement]], extras:Map[String, String], cmds:Set[String], name:String) {
    lazy val edges:Map[String, (String, ConfigElement)] = fields.flatMap { case (varName, map) => map.map { case (code, config) => (code, (varName, config)) }.toList }

    def execute(cmd:String):Execution = {
      this.readPath(cmd.split("\\."), 0)
    }
    protected def readPath(token:Array[String], i:Int):Execution = {
      if (token.size - 1 == i) {
        return Execution(token(i), Map(), List())
      }

      val (fieldName, configElem) = edges(token(i))
      val exec = configElem.readPath(token, i + 1)
      exec.addData(fieldName, configElem)
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
