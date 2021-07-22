package com.github.fsanaulla.chronicler.urlhttp

import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import com.github.fsanaulla.chronicler.urlhttp.management.{InfluxMng, UrlManagementClient}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{EitherValues, TryValues}

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 20.08.17
  */
class ShardManagementSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with EitherValues
    with TryValues
    with DockerizedInfluxDB {

  override def afterAll(): Unit = {
    influx.close()
    super.afterAll()
  }

  val testDb = "_internal"

  lazy val influx: UrlManagementClient =
    InfluxMng(s"http://$host", port, Some(creds))

  "Shard Management API" should {
    "show" should {
      "shards" in {

        influx
          .createDatabase(testDb, shardDuration = Some("1s"))
          .success
          .value
          .value shouldEqual 200

        val shards = influx.showShards.success.value.value

        shards should not be Nil
      }

//      "shards groups" in {
//        val shardGroups = influx.showShardGroups.success.value.value
//
//        shardGroups should not equal Array.empty
//
//        shardGroups shouldBe a[Array[_]]
//
//        shardGroups.head shouldBe a[ShardGroupsInfo]
//
//        influx.close() shouldEqual {}
//      }
    }
  }
}
