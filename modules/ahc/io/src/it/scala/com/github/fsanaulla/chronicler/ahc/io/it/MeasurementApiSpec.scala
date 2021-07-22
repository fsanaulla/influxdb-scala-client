package com.github.fsanaulla.chronicler.ahc.io.it

import com.github.fsanaulla.chronicler.ahc.io.{AhcIOClient, InfluxIO}
import com.github.fsanaulla.chronicler.ahc.management.{AhcManagementClient, InfluxMng}
import com.github.fsanaulla.chronicler.ahc.shared.InfluxConfig
import com.github.fsanaulla.chronicler.testing.it.{DockerizedInfluxDB, FakeEntity}
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 28.09.17
  */
class MeasurementApiSpec
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with EitherValues
    with IntegrationPatience
    with DockerizedInfluxDB {

  override def afterAll(): Unit = {
    mng.close()
    io.close()
    super.afterAll()
  }

  val db       = "db"
  val measName = "meas"

  lazy val influxConf: InfluxConfig =
    InfluxConfig(host, port, credentials = Some(creds), compress = false, None)

  lazy val mng: AhcManagementClient =
    InfluxMng(host, port, credentials = Some(creds))

  lazy val io: AhcIOClient = InfluxIO(influxConf)
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
