package com.github.fsanaulla.chronicler.akka

import _root_.akka.actor.ActorSystem
import _root_.akka.testkit.TestKit
import com.github.fsanaulla.chronicler.akka.management.{AkkaManagementClient, InfluxMng}
import com.github.fsanaulla.chronicler.core.duration._
import com.github.fsanaulla.chronicler.core.model.RetentionPolicyInfo
import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 27.07.17
  */
class RetentionPolicyManagerSpec
    extends TestKit(ActorSystem())
    with AnyFlatSpecLike
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with DockerizedInfluxDB
    with EitherValues {

  override def afterAll(): Unit = {
    influx.close()
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

  val rpDB = "db"

  lazy val influx: AkkaManagementClient =
    InfluxMng(host, port, Some(creds))

  "Retention policy API" should "create retention policy" in {
    influx.createDatabase(rpDB).futureValue.value shouldEqual 200

    influx.showDatabases().futureValue.value.contains(rpDB) shouldEqual true

    influx
      .createRetentionPolicy("test", rpDB, 2 hours, 2, Some(2 hours), default = true)
      .futureValue
      .value shouldEqual 200

    influx
      .showRetentionPolicies(rpDB)
      .futureValue
      .value
      .contains(RetentionPolicyInfo("test", "2h0m0s", "2h0m0s", 2, default = true)) shouldEqual true

  }

  it should "drop retention policy" in {
    influx.dropRetentionPolicy("autogen", rpDB).futureValue.value shouldEqual 200

    influx.showRetentionPolicies(rpDB).futureValue.value shouldEqual Seq(
      RetentionPolicyInfo("test", "2h0m0s", "2h0m0s", 2, default = true)
    )
  }

  it should "update retention policy" in {
    influx.updateRetentionPolicy("test", rpDB, Some(3 hours)).futureValue.value shouldEqual 200

    influx.showRetentionPolicies(rpDB).futureValue.value shouldEqual Seq(
      RetentionPolicyInfo("test", "3h0m0s", "2h0m0s", 2, default = true)
    )
  }

  it should "clean up everything" in {
    influx.dropRetentionPolicy("test", rpDB).futureValue.value shouldEqual 200

    influx.showRetentionPolicies(rpDB).futureValue.value.toList shouldEqual Nil

    influx.dropDatabase(rpDB).futureValue.value shouldEqual 200

    influx.showDatabases().futureValue.value.contains(rpDB) shouldEqual false
  }
}
