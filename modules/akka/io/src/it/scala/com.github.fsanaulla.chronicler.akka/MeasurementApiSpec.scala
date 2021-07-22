package com.github.fsanaulla.chronicler.akka

import _root_.akka.actor.ActorSystem
import _root_.akka.testkit.TestKit
import com.github.fsanaulla.chronicler.akka.SampleEntitys._
import com.github.fsanaulla.chronicler.akka.io.{AkkaIOClient, InfluxIO}
import com.github.fsanaulla.chronicler.akka.management.{AkkaManagementClient, InfluxMng}
import com.github.fsanaulla.chronicler.akka.shared.InfluxConfig
import com.github.fsanaulla.chronicler.testing.it.{DockerizedInfluxDB, FakeEntity}
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 28.09.17
  */
class MeasurementApiSpec
    extends TestKit(ActorSystem())
    with AnyFlatSpecLike
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with EitherValues
    with DockerizedInfluxDB {

  override def afterAll(): Unit = {
    mng.close()
    io.close()
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

  val db       = "db"
  val measName = "meas"

  lazy val influxConf: InfluxConfig =
    InfluxConfig(host, port, credentials = Some(creds), compress = false, None)

  lazy val mng: AkkaManagementClient =
    InfluxMng(host, port, credentials = Some(creds))

  lazy val io: AkkaIOClient = InfluxIO(influxConf)
  lazy val meas: io.Measurement[FakeEntity] =
    io.measurement[FakeEntity](db, measName)

  it should "write single point" in {
    mng.createDatabase(db).futureValue.value shouldEqual 200

    meas.write(singleEntity).futureValue.value shouldEqual 204

    meas.read(s"SELECT * FROM $measName").futureValue.value shouldEqual Seq(singleEntity)
  }

  it should "bulk write" in {
    meas.bulkWrite(multiEntitys).futureValue.value shouldEqual 204

    meas.read(s"SELECT * FROM $measName").futureValue.value.length shouldEqual 3
  }
}
