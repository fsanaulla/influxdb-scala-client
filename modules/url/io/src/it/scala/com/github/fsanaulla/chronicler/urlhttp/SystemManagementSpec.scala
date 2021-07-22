package com.github.fsanaulla.chronicler.urlhttp

import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import com.github.fsanaulla.chronicler.urlhttp.io.{InfluxIO, UrlIOClient}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{EitherValues, TryValues}

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 07.09.17
  */
class SystemManagementSpec
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with EitherValues
    with TryValues
    with DockerizedInfluxDB {

  override def afterAll(): Unit = {
    influx.close()
    super.afterAll()
  }

  lazy val influx: UrlIOClient = InfluxIO(s"http://$host", port, Some(creds))

  it should "ping InfluxDB" in {
    val result = influx.ping.success.value.value
    result.build shouldEqual "OSS"
    result.version shouldEqual version
  }
}
