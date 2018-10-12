package com.github.fsanaulla.chronicler.urlhttp

import com.github.fsanaulla.chronicler.core.model.InfluxConfig
import com.github.fsanaulla.chronicler.testing.it.ResultMatchers._
import com.github.fsanaulla.chronicler.testing.it.{DockerizedInfluxDB, FakeEntity}
import com.github.fsanaulla.chronicler.testing.unit.FlatSpecWithMatchers
import com.github.fsanaulla.chronicler.urlhttp.SampleEntitys._
import com.github.fsanaulla.chronicler.urlhttp.io.api.Measurement
import com.github.fsanaulla.chronicler.urlhttp.io.{Influx, UrlIOClient}
import com.github.fsanaulla.chronicler.urlhttp.management.{UrlManagementClient, Influx => MngInflux}
import org.scalatest.TryValues

/**
  * Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 28.09.17
  */
class MeasurementApiSpec extends FlatSpecWithMatchers with DockerizedInfluxDB with TryValues {

  val safeDB = "db"
  val measName = "meas"

  lazy val influxConf =
    InfluxConfig(host, port, credentials = Some(creds), gzipped = false)

  lazy val management: UrlManagementClient =
    MngInflux.management(influxConf)

  lazy val io: UrlIOClient =
    Influx.io(influxConf)

  lazy val meas: Measurement[FakeEntity] = io.measurement[FakeEntity](safeDB, measName)

  "Measurement[FakeEntity]" should "make single write" in {
    management.createDatabase(safeDB).success.value shouldEqual OkResult

    meas.write(singleEntity).success.value shouldEqual NoContentResult

    meas.read(s"SELECT * FROM $measName")
      .success.value
      .queryResult shouldEqual Array(singleEntity)
  }

  it should "make safe bulk write" in {
    meas.bulkWrite(multiEntitys).success.value shouldEqual NoContentResult

    meas.read(s"SELECT * FROM $measName")
      .success.value
      .queryResult
      .length shouldEqual 3

    management.close() shouldEqual {}
    io.close() shouldEqual {}
  }
}