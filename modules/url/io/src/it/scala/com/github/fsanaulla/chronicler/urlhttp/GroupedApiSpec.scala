package com.github.fsanaulla.chronicler.urlhttp

import com.github.fsanaulla.chronicler.core.either
import com.github.fsanaulla.chronicler.core.either.EitherOps
import com.github.fsanaulla.chronicler.core.enums.Precisions
import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import com.github.fsanaulla.chronicler.urlhttp.io.{InfluxIO, UrlIOClient}
import com.github.fsanaulla.chronicler.urlhttp.management.{InfluxMng, UrlManagementClient}
import com.github.fsanaulla.chronicler.urlhttp.shared.InfluxConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{EitherValues, TryValues}
import org.typelevel.jawn.ast.{JArray, JNum, JString}

import scala.io.Source
import scala.util.{Failure, Success}

// https://github.com/fsanaulla/chronicler/issues/193
class GroupedApiSpec
    extends AnyWordSpec
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

  val dbName = "mydb"

  lazy val influxConf: InfluxConfig =
    InfluxConfig(s"http://$host", port, Some(creds))
  lazy val mng: UrlManagementClient =
    InfluxMng(influxConf)
  lazy val io: UrlIOClient =
    InfluxIO(influxConf)

  "Grouped Api" should {
    "prepare data for testing" in {
      for {
        _ <- mng.createDatabase(dbName)

        db = io.database(dbName)

        data = Source
          .fromInputStream(getClass.getResourceAsStream("/h2feet_sample.txt"))
          .getLines()
          .sliding(500, 500)

        wr <- either
          .seq[Throwable, Int](
            data
              .map(db.bulkWriteNative(_, precision = Precisions.Seconds))
              .flatMap(_.toOption)
              .toSeq
          ) match {
          case Right(b) => Success(b)
          case Left(a)  => Failure(a)
        }

        count <- db.readJson("SELECT * FROM h2o_feet")
      } yield {
        wr.forall(_ == 204) shouldEqual true
        count.mapRight(_.length > 0) shouldEqual Right(true)
      }
    }

    "group by" should {
      lazy val db = io.database(dbName)

      "multiple tags" in {
        val sql = """
                    |select mean("water_level") 
                    |from h2o_feet 
                    |group by "location", time(10d) fill(none)
                    |""".stripMargin

        val Success(groupedResult) = db
          .readGroupedJson(sql)

        groupedResult.mapRight(
          _.map { case (tags, value) => tags.toList -> value.toList }.toList
        ) shouldEqual Right(
          List(
            (
              List("coyote_creek"),
              List(
                JArray(Array(JString("2015-08-13T00:00:00Z"), JNum(5.255175833333332))),
                JArray(Array(JString("2015-08-23T00:00:00Z"), JNum(5.282439166666671))),
                JArray(Array(JString("2015-09-02T00:00:00Z"), JNum(5.365102959566481))),
                JArray(Array(JString("2015-09-12T00:00:00Z"), JNum(5.543609345794391)))
              )
            ),
            (
              List("santa_monica"),
              List(
                JArray(Array(JString("2015-08-13T00:00:00Z"), JNum(3.3572483333333363))),
                JArray(Array(JString("2015-08-23T00:00:00Z"), JNum(3.374660833333338))),
                JArray(Array(JString("2015-09-02T00:00:00Z"), JNum(3.527653171953253))),
                JArray(Array(JString("2015-09-12T00:00:00Z"), JNum(3.887266586248496)))
              )
            )
          )
        )
      }

      "single tag" in {
        val sql = """
                    |select mean("water_level") 
                    |from h2o_feet 
                    |group by "location"
                    |""".stripMargin

        val Success(groupedResult) = db
          .readGroupedJson(sql)

        groupedResult.mapRight(
          _.map { case (tags, value) => tags.toList -> value.toList }.toList
        ) shouldEqual Right(
          List(
            (
              List("coyote_creek"),
              List(JArray(Array(JString("1970-01-01T00:00:00Z"), JNum(5.359342451341401))))
            ),
            (
              List("santa_monica"),
              List(JArray(Array(JString("1970-01-01T00:00:00Z"), JNum(3.530863470081006))))
            )
          )
        )
      }
    }
  }
}
