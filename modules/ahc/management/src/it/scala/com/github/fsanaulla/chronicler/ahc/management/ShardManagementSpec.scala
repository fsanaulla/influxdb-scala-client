package com.github.fsanaulla.chronicler.ahc.management

import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 20.08.17
  */
class ShardManagementSpec
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with EitherValues
    with IntegrationPatience
    with DockerizedInfluxDB {

  override def afterAll(): Unit = {
    influx.close()
    super.afterAll()
  }

  val testDb = "_internal"

  lazy val influx: AhcManagementClient =
    InfluxMng(host, port, Some(creds))

  "Shard Management API" should "show shards" in {

    influx.createDatabase(testDb, shardDuration = Some("1s")).futureValue.value shouldEqual 200

    val shards = influx.showShards.futureValue.value

    shards should not be Nil
  }

//  it should "show shards groupe" in {
//
//    val shardGroups = influx.showShardGroups.futureValue.value
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
