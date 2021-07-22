package com.github.fsanaulla.chronicler.ahc.io.it

import com.github.fsanaulla.chronicler.ahc.io.{AhcIOClient, InfluxIO}
import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 07.09.17
  */
class SystemManagementSpec
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

  lazy val influx: AhcIOClient =
    InfluxIO(host, port, Some(creds))

  it should "ping InfluxDB" in {
    val result = influx.ping.futureValue.value
    result.build shouldEqual "OSS"
    result.version shouldEqual version
  }
}
