package com.github.fsanaulla.chronicler.urlhttp

import java.nio.file.Paths

import com.github.fsanaulla.chronicler.core.enums.Epochs
import com.github.fsanaulla.chronicler.core.model.InfluxReader
import com.github.fsanaulla.chronicler.macros.Influx
import com.github.fsanaulla.chronicler.macros.annotations.reader.epoch
import com.github.fsanaulla.chronicler.macros.annotations.{field, tag, timestamp}
import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import com.github.fsanaulla.chronicler.urlhttp.StreamingApiSpec._
import com.github.fsanaulla.chronicler.urlhttp.io.{
  InfluxIO,
  UrlDatabaseApi,
  UrlIOClient,
  UrlMeasurementApi
}
import com.github.fsanaulla.chronicler.urlhttp.management.{InfluxMng, UrlManagementClient}
import com.github.fsanaulla.chronicler.urlhttp.shared.InfluxConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, TryValues}
import org.typelevel.jawn.ast.JValue

import scala.collection.mutable.ArrayBuffer
import scala.util.Success

class StreamingApiSpec
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

  val testDB   = "db"
  val measName = "test1"

  lazy val influxConf: InfluxConfig =
    InfluxConfig(s"http://$host", port, Some(creds))

  lazy val mng: UrlManagementClient =
    InfluxMng(influxConf)

  lazy val io: UrlIOClient =
    InfluxIO(influxConf)

  lazy val db: UrlDatabaseApi             = io.database(testDB)
  lazy val meas: UrlMeasurementApi[Point] = io.measurement[Point](testDB, measName)

  it should "read chunked json result" in {
    mng.createDatabase(testDB).success.value.value shouldEqual 200

    db.writeFromFile(Paths.get(getClass.getResource("/points.txt").getPath))
      .success
      .value
      .value shouldEqual 204

    val arr = new ArrayBuffer[JValue](3)

    val Success(iter) = db.readChunkedJson(s"SELECT * FROM $measName", chunkSize = 2)

    while (iter.hasNext) {
      val nxt = iter.next()

      nxt match {
        case Right(vl) => vl.foreach(arr += _)
        case _         => fail()
      }
    }

    arr.size shouldEqual 3
  }

  it should "read typed chunked response" in {

    val arr = new ArrayBuffer[Point](3)

    val Success(iterator) = meas.readChunked(
      s"SELECT * FROM $measName",
      epoch = Epochs.Milliseconds,
      chunkSize = 3
    )

    while (iterator.hasNext) {
      val nxt = iterator.next()

      nxt match {
        case Right(vl) =>
          vl.foreach(arr += _)
        case Left(err) =>
          fail(err)
      }
    }

    arr.size shouldEqual 3
  }

  it should "read chunked json as one batch" in {
    val arr = new ArrayBuffer[JValue](3)

    val Success(iter) = db.readChunkedJson(s"SELECT * FROM $measName")

    val nxt = iter.next()
    nxt match {
      case Right(vl) => vl.foreach(arr += _)
      case _         => fail()
    }

    arr.size shouldEqual 3
    iter.hasNext shouldEqual false
  }
}

object StreamingApiSpec {
  final case class Point(
      @tag direction: Option[String],
      @tag host: String,
      @tag region: Option[String],
      @field value: Double,
      @epoch @timestamp time: Long
  )

  implicit val rd: InfluxReader[Point] = Influx.reader
}
