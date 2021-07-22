package com.github.fsanaulla.chronicler.urlhttp

import java.nio.file.Paths

import com.github.fsanaulla.chronicler.core.enums.Epochs
import com.github.fsanaulla.chronicler.core.model.Point
import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import com.github.fsanaulla.chronicler.urlhttp.SampleEntitys._
import com.github.fsanaulla.chronicler.urlhttp.io.{InfluxIO, UrlIOClient}
import com.github.fsanaulla.chronicler.urlhttp.management.{InfluxMng, UrlManagementClient}
import com.github.fsanaulla.chronicler.urlhttp.shared.InfluxConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, TryValues}
import org.typelevel.jawn.ast.{JArray, JNum, JString, JValue}

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 02.03.18
  */
class DatabaseApiSpec
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

  import DatabaseApiSpec._

  val testDB = "db"

  lazy val influxConf: InfluxConfig =
    InfluxConfig(s"http://$host", port, Some(creds))

  lazy val mng: UrlManagementClient =
    InfluxMng(influxConf)

  lazy val io: UrlIOClient =
    InfluxIO(influxConf)

  lazy val db: io.Database = io.database(testDB)

  it should "write data from file" in {
    mng.createDatabase(testDB).success.value.value shouldEqual 200

    db.writeFromFile(Paths.get(getClass.getResource("/points.txt").getPath))
      .success
      .value
      .value shouldEqual 204

    db.readJson("SELECT * FROM test1").success.value.value.length shouldEqual 3
  }

  it should "write 2 points represented entities" in {
    val point1 = Point("test2")
      .addTag("sex", "Male")
      .addTag("firstName", "Martin")
      .addTag("lastName", "Odersky")
      .addField("age", 54)

    val point2 = Point("test2")
      .addTag("sex", "Male")
      .addTag("firstName", "Jame")
      .addTag("lastName", "Franko")
      .addField("age", 36)

    db.writePoint(point1).success.value.value shouldEqual 204

    db.readJson("SELECT * FROM test2", epoch = Epochs.Nanoseconds)
      .success
      .value
      .value
      // skip timestamp
      .map(jarr => jarr.copy(vs = jarr.vs.tail)) shouldEqual Array(
      JArray(Array(JNum(54), JString("Martin"), JString("Odersky"), JString("Male")))
    )

    db.bulkWritePoints(Array(point1, point2)).success.value.value shouldEqual 204

    db.readJson("SELECT * FROM test2", epoch = Epochs.Nanoseconds)
      .success
      .value
      .value
      // skip timestamp
      .map(jarr => jarr.copy(vs = jarr.vs.tail)) shouldEqual Array(
      JArray(Array(JNum(54), JString("Martin"), JString("Odersky"), JString("Male"))),
      JArray(Array(JNum(36), JString("Jame"), JString("Franko"), JString("Male"))),
      JArray(Array(JNum(54), JString("Martin"), JString("Odersky"), JString("Male")))
    )
  }

  it should "retrieve multiple request" in {
    val multiQuery = db
      .bulkReadJson(
        Array(
          "SELECT * FROM test2",
          "SELECT * FROM test2 WHERE age < 40"
        )
      )
      .get

    multiQuery.value.length shouldEqual 2
    multiQuery.value shouldBe a[Array[_]]

    multiQuery.value.head.length shouldEqual 3
    multiQuery.value.head shouldBe a[Array[_]]
    multiQuery.value.head.head shouldBe a[JArray]

    multiQuery.value.last.length shouldEqual 1
    multiQuery.value.last shouldBe a[Array[_]]
    multiQuery.value.last.head shouldBe a[JArray]

    multiQuery.value
      .map(_.map(_.arrayValue.get.tail)) shouldEqual largeMultiJsonEntity.map(
      _.map(_.arrayValue.get.tail)
    )
  }

  it should "write native" in {
    db.writeNative("test3,sex=Male,firstName=Jame,lastName=Lannister age=48")
      .success
      .value
      .value shouldEqual 204

    db.readJson("SELECT * FROM test3")
      .success
      .value
      .value
      .map(jarr => jarr.copy(vs = jarr.vs.tail)) shouldEqual Array(
      JArray(Array(JNum(48), JString("Jame"), JString("Lannister"), JString("Male")))
    )

    db.bulkWriteNative(
      Seq(
        "test4,sex=Male,firstName=Jon,lastName=Snow age=24",
        "test4,sex=Female,firstName=Deny,lastName=Targaryen age=25"
      )
    ).success
      .value
      .value shouldEqual 204

    db.readJson("SELECT * FROM test4")
      .success
      .value
      .value
      .map(jarr => jarr.copy(vs = jarr.vs.tail)) shouldEqual Array(
      JArray(Array(JNum(25), JString("Deny"), JString("Targaryen"), JString("Female"))),
      JArray(Array(JNum(24), JString("Jon"), JString("Snow"), JString("Male")))
    )
  }

  it should "write escaped value" in {
    val p = Point("test6")
      .addTag("key,", "value,")
      .addField("field=key", 1)

    db.writePoint(p).success.value.value shouldEqual 204

    db.readJson("SELECT * FROM test6").success.value.value.length shouldEqual 1
  }

  it should "validate empty response" in {
    db.readJson("SELECT * FROM test7").success.value.value.length shouldEqual 0
  }
}

object DatabaseApiSpec {
  implicit final class JawnOps(private val jv: JValue) {

    def arrayValue: Option[Array[JValue]] = jv match {
      case JArray(arr) => Some(arr)
      case _           => None
    }
  }
}
