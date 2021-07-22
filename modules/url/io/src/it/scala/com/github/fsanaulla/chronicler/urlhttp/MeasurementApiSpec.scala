package com.github.fsanaulla.chronicler.urlhttp

import com.github.fsanaulla.chronicler.testing.it.{DockerizedInfluxDB, FakeEntity}
import com.github.fsanaulla.chronicler.urlhttp.SampleEntitys._
import com.github.fsanaulla.chronicler.urlhttp.io.{InfluxIO, UrlIOClient}
import com.github.fsanaulla.chronicler.urlhttp.management.{InfluxMng, UrlManagementClient}
import com.github.fsanaulla.chronicler.urlhttp.shared.InfluxConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, TryValues}

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 28.09.17
  */
class MeasurementApiSpec
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with EitherValues
    with TryValues
    with DockerizedInfluxDB {

  override def afterAll(): Unit = {
    mng.close()
    io.close()
    super.afterAll()
  }

  val db       = "db"
  val measName = "meas"

  lazy val influxConf: InfluxConfig =
    InfluxConfig(s"http://$host", port, Some(creds))

  lazy val mng: UrlManagementClient =
    InfluxMng(influxConf)

  lazy val io: UrlIOClient = InfluxIO(influxConf)

  lazy val meas: io.Measurement[FakeEntity] =
    io.measurement[FakeEntity](db, measName)

  it should "write single point" in {
    mng.createDatabase(db).success.value.value shouldEqual 200

    meas.write(singleEntity).success.value.value shouldEqual 204

    meas.read(s"SELECT * FROM $measName").success.value.value shouldEqual Seq(singleEntity)
  }

  it should "bulk write" in {
    meas.bulkWrite(multiEntitys).success.value.value shouldEqual 204

    meas.read(s"SELECT * FROM $measName").success.value.value.length shouldEqual 3
  }
}
