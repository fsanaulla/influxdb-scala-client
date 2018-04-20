import sbt._

/**
  * Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 28.08.17
  */
object Dependencies {

  // core
  final val jawn = "org.spire-math" %% "jawn-ast" % "0.12.1"
  final val enums = "com.beachape" %% "enumeratum" % "1.5.13"

  // akka-http
  final val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.11" % Provided
  final val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.1.1"

  // async-http
  final val asyncHttp = "com.softwaremill.sttp" %% "async-http-client-backend-future" % "1.1.12"

  // macros
  final def scalaReflect(scalaVersion: String): ModuleID = "org.scala-lang" % "scala-reflect" % scalaVersion

  // for testing
  final val embedInflux = "com.github.fsanaulla" %% "scalatest-embedinflux" % "0.1.6" % Test

  final val coreDep = Seq(enums, jawn, embedInflux)
  final val akkaDep = Seq(akkaStream, akkaHttp)
}
