import sbt.Keys.resolvers

name := "chronicler"

organization := "com.github.fsanaulla"

scalaVersion := "2.12.2"

crossScalaVersions := Seq(scalaVersion.value, "2.11.10")

scalacOptions ++= Seq(
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Xplugin-require:macroparadise"
)

// Developer section
homepage := Some(url("https://github.com/fsanaulla/chronicler"))

licenses += "MIT" -> url("https://opensource.org/licenses/MIT")

scmInfo := Some(
  ScmInfo(
    url("https://github.com/fsanaulla/chronicler"),
    "https://github.com/fsanaulla/chronicler.git")
)

developers += Developer(
  id = "fsanaulla",
  name = "Faiaz Sanaulla",
  email = "fayaz.sanaulla@gmail.com",
  url = url("https://github.com/fsanaulla")
)

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  sys.env.getOrElse("SONATYPE_LOGIN", ""),
  sys.env.getOrElse("SONATYPE_PASS", "")
)

// Dependencies section
libraryDependencies ++= Dependencies.dep

// Coverage section
coverageMinimum := Coverage.min
coverageExcludedPackages := Coverage.exclude

// Publish section
useGpg := true

pgpReadOnly := false

releaseCrossBuild := true

publishArtifact in Test := false

publishMavenStyle := true

pomIncludeRepository := (_ => false)

releaseProcess := Release.releaseSteps

resolvers ++= Dependencies.projectResolvers