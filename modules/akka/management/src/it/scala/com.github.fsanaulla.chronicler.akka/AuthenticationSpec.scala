package com.github.fsanaulla.chronicler.akka

import _root_.akka.actor.ActorSystem
import _root_.akka.testkit.TestKit
import com.github.fsanaulla.chronicler.akka.management.{AkkaManagementClient, InfluxMng}
import com.github.fsanaulla.chronicler.core.enums.Privileges
import com.github.fsanaulla.chronicler.core.model.{InfluxException, UserPrivilegesInfo}
import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 17.08.17
  */
class AuthenticationSpec
    extends TestKit(ActorSystem())
    with AnyFlatSpecLike
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with EitherValues
    with DockerizedInfluxDB {

  override def afterAll(): Unit = {
    influx.close()
    authInflux.close()
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

  val userDB    = "db"
  val userName  = "some_user"
  val userPass  = "some_user_pass"
  val userNPass = "some_new_user_pass"

  val admin     = "admin"
  val adminPass = "admin"

  lazy val influx: AkkaManagementClient =
    InfluxMng(host, port)

  lazy val authInflux: AkkaManagementClient =
    InfluxMng(host = host, port = port, credentials = Some(creds))

  "AuthenticationUserManagement" should "create admin user " in {
    influx.showUsers.futureValue.left.value shouldBe a[InfluxException]
  }

  it should "create database" in {
    authInflux.createDatabase(userDB).futureValue.value shouldEqual 200
  }

  it should "create user" in {
    authInflux.createUser(userName, userPass).futureValue.value shouldEqual 200
    authInflux.showUsers.futureValue.value.exists(_.username == userName) shouldEqual true
  }

  it should "set user password" in {
    authInflux.setUserPassword(userName, userNPass).futureValue.value shouldEqual 200
  }

  it should "set user privileges" in {
    authInflux
      .setPrivileges(userName, userDB, Privileges.READ)
      .futureValue
      .value shouldEqual 200
  }

  it should "get user privileges" in {
    val userPrivs = authInflux.showUserPrivileges(userName).futureValue.value

    userPrivs.length shouldEqual 1
    userPrivs.exists { upi =>
      upi.database == userDB && upi.privilege == Privileges.withName("READ")
    } shouldEqual true
  }

  it should "revoke user privileges" in {
    authInflux
      .revokePrivileges(userName, userDB, Privileges.READ)
      .futureValue
      .value shouldEqual 200
    authInflux.showUserPrivileges(userName).futureValue.value shouldEqual Array(
      UserPrivilegesInfo(userDB, Privileges.NO_PRIVILEGES)
    )
  }

  it should "drop user" in {
    authInflux.dropUser(userName).futureValue.value shouldEqual 200
    authInflux.dropUser(admin).futureValue.value shouldEqual 200
  }
}
