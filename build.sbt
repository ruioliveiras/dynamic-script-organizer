
name := "infrastructureConnector"

version := "0.1"

organization := "JumiaOpenSource"

scalaVersion := "2.11.11"

sbtVersion := "0.13.15"

scalaSource in Compile := baseDirectory.value / "src/core"

libraryDependencies += "com.typesafe" % "config" % "1.3.1"
libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
libraryDependencies += "io.suzaku" %% "boopickle" % "1.2.6"
libraryDependencies += "com.github.spullara.mustache.java" % "compiler" % "0.9.5"


mainClass in assembly := Some("Main")
assemblyJarName in assembly := "../../dist/infra.jar"
