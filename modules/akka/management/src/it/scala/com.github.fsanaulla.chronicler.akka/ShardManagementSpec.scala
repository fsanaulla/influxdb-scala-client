package com.github.fsanaulla.chronicler.akka

import _root_.akka.actor.ActorSystem
import _root_.akka.testkit.TestKit
import com.github.fsanaulla.chronicler.akka.management.{AkkaManagementClient, InfluxMng}
import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 20.08.17
  */
class ShardManagementSpec
    extends TestKit(ActorSystem())
    with AnyFlatSpecLike
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with EitherValues
    with DockerizedInfluxDB {

  override def afterAll(): Unit = {
    influx.close()
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

  val testDb = "_internal"

  lazy val influx: AkkaManagementClient =
    InfluxMng(host, port, Some(creds))

  "shard operations" should "show shards" in {

    influx.createDatabase(testDb, shardDuration = Some("1s")).futureValue.value shouldEqual 200

    val shards = influx.showShards.futureValue.value

    shards should not be Nil
  }

//  it should "show shards groupe" in {
//
//    val shardGroups = influx.showShardGroups.futureValue.right.get
//
//    shardGroups should not equal Nil
//
//    shardGroups shouldBe a[Array[_]]
//
//    shardGroups.head shouldBe a[ShardGroupsInfo]
//
//    influx.close() shouldEqual {}
//  }
}
