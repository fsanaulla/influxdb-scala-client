package com.github.fsanaulla.chronicler.akka

import _root_.akka.actor.ActorSystem
import _root_.akka.testkit.TestKit
import com.github.fsanaulla.chronicler.akka.io.{AkkaIOClient, InfluxIO}
import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 07.09.17
  */
class SystemManagementSpec
    extends TestKit(ActorSystem())
    with AnyFlatSpecLike
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with EitherValues
    with DockerizedInfluxDB {

  override def afterAll(): Unit = {
    io.close()
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

  lazy val io: AkkaIOClient =
    InfluxIO(host, port, Some(creds))

  it should "ping InfluxDB" in {
    val result = io.ping.futureValue.value
    result.build shouldEqual "OSS"
    result.version shouldEqual version
  }
}
