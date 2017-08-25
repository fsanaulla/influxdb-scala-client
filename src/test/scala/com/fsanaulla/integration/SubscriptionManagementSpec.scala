package com.fsanaulla.integration

import com.fsanaulla.InfluxClient
import com.fsanaulla.model.Subscription
import com.fsanaulla.utils.InfluxDuration._
import com.fsanaulla.utils.TestHelper._
import com.fsanaulla.utils.TestSpec
import com.fsanaulla.utils.constants.Destinations

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 21.08.17
  */
class SubscriptionManagementSpec extends TestSpec {

  val subName = "subs"
  val dbName = "subs_db"
  val rpName = "subs_rp"
  val destType  = Destinations.ANY
  val newDestType = Destinations.ALL
  val hosts = Seq("udp://h1.example.com:9090", "udp://h2.example.com:9090")
  val subscription = Subscription(rpName, subName, destType, hosts)
  val newSubscription: Subscription = subscription.copy(destType = newDestType)

  val duration: String = 1.hours + 30.minutes

  "subs operation" should "correctly work" in {

    // INIT INFLUX CLIENT
    val influx = InfluxClient(host = influxHost, username = credentials.username, password = credentials.password)

    influx.createDatabase(dbName).futureValue shouldEqual OkResult

    influx.createRetentionPolicy(rpName, dbName, duration, 1, Some(duration)).futureValue shouldEqual OkResult

    influx.showDatabases().futureValue.queryResult.contains(dbName) shouldEqual true

    influx.createSubscription(subName, dbName, rpName, destType, hosts).futureValue shouldEqual OkResult

    influx.showSubscriptions(dbName).futureValue shouldEqual Seq(subscription)

    influx.updateSubscription(subName, dbName, rpName, newDestType, hosts).futureValue shouldEqual OkResult

    influx.showSubscriptions(dbName).futureValue.queryResult shouldEqual Seq(newSubscription)

    influx.dropSubscription(subName, dbName, rpName).futureValue shouldEqual OkResult

    influx.showSubscriptions(dbName).futureValue.queryResult shouldEqual Nil

    influx.dropRetentionPolicy(rpName, dbName).futureValue shouldEqual OkResult

    influx.dropDatabase(dbName).futureValue shouldEqual OkResult

    influx.close()

  }

}