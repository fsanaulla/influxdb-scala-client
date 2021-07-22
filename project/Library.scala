import sbt._

/**
  * Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 28.08.17
  */
object Library {

  object Versions {
    val request = "0.2.0"

    object Akka {
      val akka     = "2.5.32"
      val akkaHttp = "10.1.13"
    }

    object Testing {
      val scalaTest            = "3.2.3"
      val scalaCheck           = "1.14.0"
      val scalaCheckGenerators = "0.2.0"
    }
  }

  val scalaTest   = "org.scalatest"     %% "scalatest"       % Versions.Testing.scalaTest
  val scalaCheck  = "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0"
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit"    % Versions.Akka.akka

  def macroDeps(scalaVersion: String): List[ModuleID] =
    "org.scala-lang" % "scala-reflect" % scalaVersion :: List(scalaTest, scalaCheck).map(
      _ % Test
    )

  // testing
  val testingDeps: List[ModuleID] = List(
    scalaTest,
    "org.jetbrains"      % "annotations" % "16.0.3",
    "org.testcontainers" % "influxdb"    % "1.15.1" exclude ("org.jetbrains", "annotations") exclude ("org.slf4j", "slf4j-api"),
    "org.slf4j"          % "slf4j-api"   % "1.7.25"
  )

  val coreTestDeps: List[ModuleID] = List(scalaTest, scalaCheck).map(_ % Test)

  // akka-http
  // format: off
  val akkaDep: List[ModuleID] = List(
    "com.typesafe.akka" %% "akka-stream" % Versions.Akka.akka exclude ("com.typesafe", "config"),
    "com.typesafe"      %  "config"       % "1.4.1",
    "com.typesafe.akka" %% "akka-http"   % Versions.Akka.akkaHttp,
    akkaTestKit % Test
  )

  // async-http
  val asyncDeps: List[ModuleID] = List(
    "org.asynchttpclient"    % "async-http-client"   % "2.12.1",
    "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.0"
  )

  // looks like a shit, but need to keep it until spark on 2.12 will become stable
  def requestScala(scalaVersion: String): ModuleID = {
    val requestVersion = scalaVersion match {
      case v if v.startsWith("2.11") => "0.1.9"
      case _                         => Versions.request
    }

    "com.lihaoyi" %% "requests" % requestVersion
  }
}
