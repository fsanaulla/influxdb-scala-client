import sbt.{Def, Defaults, config, inConfig}
import sbt._

object Settings {
  val CompileTimeIntegrationTest = config("it") extend Test
  val PropertyTest               = config("pt") extend Test

  val propertyTestSettings: Seq[Def.Setting[_]] =
    inConfig(PropertyTest)(Defaults.testSettings)
}
