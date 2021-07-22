package com.github.fsanaulla.chronicler.testing.it

import com.github.fsanaulla.chronicler.core.model.InfluxCredentials
import org.scalatest.{BeforeAndAfterAll, Suite}
import org.testcontainers.containers.InfluxDBContainer
import org.testcontainers.containers.output.ToStringConsumer

trait DockerizedInfluxDB extends BeforeAndAfterAll { self: Suite =>

  protected def version: String = sys.env.getOrElse("INFLUXDB_VERSION", "1.7.3")
  protected val _influx: InfluxDBContainer[Nothing] =
    new InfluxDBContainer(version).withLogConsumer(new ToStringConsumer)

  /** Credentials for influx */
  final val creds: InfluxCredentials = InfluxCredentials("admin", "password")

  /** host address */
  def host: String = _influx.getContainerIpAddress

  /** mapped port */
  def port: Int = _influx.getLivenessCheckPortNumbers.toArray.head.asInstanceOf[Int]

  def mappedPort(mappedPort: Int): Integer = _influx.getMappedPort(mappedPort)

  def beforeStartContainer(container: InfluxDBContainer[Nothing]): InfluxDBContainer[Nothing] =
    container

  override def beforeAll(): Unit = {
    super.beforeAll()
    _influx.start()
    _influx.followOutput(new ToStringConsumer)
    beforeStartContainer(_influx) /*.start()*/
  }

  override def afterAll(): Unit = {
    super.afterAll()
    _influx.stop()
  }
}
