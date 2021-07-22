package com.github.fsanaulla.chronicler.akka

import java.nio.file.Paths

import _root_.akka.actor.ActorSystem
import _root_.akka.testkit.TestKit
import com.github.fsanaulla.chronicler.akka.DatabaseApiSpec._
import com.github.fsanaulla.chronicler.akka.SampleEntitys._
import com.github.fsanaulla.chronicler.akka.io.{AkkaIOClient, InfluxIO}
import com.github.fsanaulla.chronicler.akka.management.{AkkaManagementClient, InfluxMng}
import com.github.fsanaulla.chronicler.akka.shared.InfluxConfig
import com.github.fsanaulla.chronicler.core.either.EitherOps
import com.github.fsanaulla.chronicler.core.enums.Epochs
import com.github.fsanaulla.chronicler.core.model.Point
import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.typelevel.jawn.ast.{JArray, JNum, JString, JValue}

import scala.concurrent.ExecutionContext.Implicits.global

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 02.03.18
  */
class DatabaseApiSpec
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

  val testDB = "db"

  lazy val influxConf: InfluxConfig =
    InfluxConfig(host, port, credentials = Some(creds), compress = false, None)

  lazy val mng: AkkaManagementClient =
    InfluxMng(host, port, credentials = Some(creds))

  lazy val io: AkkaIOClient =
    InfluxIO(influxConf)

  lazy val db: io.Database = io.database(testDB)

  it should "write data from file" in {
    mng.createDatabase(testDB).futureValue shouldEqual Right(200)

    db.writeFromFile(Paths.get(getClass.getResource("/points.txt").getPath))
      .futureValue shouldEqual Right(204)

    db.readJson("SELECT * FROM test1").futureValue.mapRight(_.length) shouldEqual Right(3)
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

    db.writePoint(point1).futureValue shouldEqual Right(204)

    db.readJson("SELECT * FROM test2", epoch = Epochs.Nanoseconds)
      .futureValue
      .mapRight(_.map(arr => arr.copy(vs = arr.vs.tail)).toList) shouldEqual Right(
      List(
        JArray(Array(JNum(54), JString("Martin"), JString("Odersky"), JString("Male")))
      )
    )

    db.bulkWritePoints(Array(point1, point2)).futureValue shouldEqual Right(204)

    db.readJson("SELECT * FROM test2", epoch = Epochs.Nanoseconds)
      .futureValue
      .value
      // skip timestamp
      .map(jarr => jarr.copy(vs = jarr.vs.tail)) shouldEqual Array(
      JArray(Array(JNum(54), JString("Martin"), JString("Odersky"), JString("Male"))),
      JArray(Array(JNum(36), JString("Jame"), JString("Franko"), JString("Male"))),
      JArray(Array(JNum(54), JString("Martin"), JString("Odersky"), JString("Male")))
    )
  }

  it should "retrieve multiple request" in {
    val Right(multiQuery) = db
      .bulkReadJson(
        Array(
          "SELECT * FROM test2",
          "SELECT * FROM test2 WHERE age < 40"
        )
      )
      .futureValue
      .mapRight(_.map(_.toList).toList)

    multiQuery.length shouldEqual 2
    multiQuery shouldBe a[List[_]]

    multiQuery.headOption.map(_.length) shouldEqual Some(3)
    multiQuery.headOption.map(_ shouldBe a[List[_]]) shouldEqual Some(succeed)
    multiQuery.headOption.flatMap(_.headOption).map(_ shouldBe a[JArray]) shouldEqual Some(succeed)

    multiQuery.last.length shouldEqual 1
    multiQuery.last shouldBe a[List[_]]
    multiQuery.last.head shouldBe a[JArray]

    multiQuery
      .map(_.flatMap(_.arrayValue.map(_.tail.toList)))
      .shouldEqual(largeMultiJsonEntity.map(_.flatMap(_.arrayValue.map(_.tail.toList)).toList))
  }

  it should "write native" in {
    db.writeNative("test3,sex=Male,firstName=Jame,lastName=Lannister age=48")
      .futureValue
      .value shouldEqual 204

    db.readJson("SELECT * FROM test3")
      .futureValue
      .value
      .map(jarr => jarr.copy(vs = jarr.vs.tail)) shouldEqual Array(
      JArray(Array(JNum(48), JString("Jame"), JString("Lannister"), JString("Male")))
    )

    db.bulkWriteNative(
      Seq(
        "test4,sex=Male,firstName=Jon,lastName=Snow age=24",
        "test4,sex=Female,firstName=Deny,lastName=Targaryen age=25"
      )
    ).futureValue
      .value shouldEqual 204

    db.readJson("SELECT * FROM test4")
      .futureValue
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

    db.writePoint(p).futureValue.value shouldEqual 204

    db.readJson("SELECT * FROM test6").futureValue.value.length shouldEqual 1
  }

  it should "validate empty response" in {
    db.readJson("SELECT * FROM test7").futureValue.value.length shouldEqual 0
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
