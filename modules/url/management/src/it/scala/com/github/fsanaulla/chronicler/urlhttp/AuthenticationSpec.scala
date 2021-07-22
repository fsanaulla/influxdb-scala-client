package com.github.fsanaulla.chronicler.urlhttp

import com.github.fsanaulla.chronicler.core.enums.Privileges
import com.github.fsanaulla.chronicler.core.model.{InfluxException, UserPrivilegesInfo}
import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import com.github.fsanaulla.chronicler.urlhttp.management.{InfluxMng, UrlManagementClient}
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 17.08.17
  */
class AuthenticationSpec
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with DockerizedInfluxDB
    with EitherValues {

  override def afterAll(): Unit = {
    influx.close()
    super.afterAll()
  }

  val userDB    = "db"
  val userName  = "some_user"
  val userPass  = "some_user_pass"
  val userNPass = "some_new_user_pass"

  val admin     = "admin"
  val adminPass = "admin"

  lazy val influx: UrlManagementClient = {
    InfluxMng(s"http://$host", port)
  }

  lazy val authInflux: UrlManagementClient =
    InfluxMng(s"http://$host", port, Some(creds))

  "Authenticated User Management API" should "create admin user " in {
    influx.showUsers.get.left.value shouldBe a[InfluxException]
  }

  it should "create database" in {
    authInflux.createDatabase(userDB).get.value shouldEqual 200
  }

  it should "create user" in {
    authInflux.createUser(userName, userPass).get.value shouldEqual 200
    authInflux.showUsers.get.value.exists(_.username == userName) shouldEqual true
  }

  it should "set user password" in {
    authInflux.setUserPassword(userName, userNPass).get.value shouldEqual 200
  }

  it should "set user privileges" in {
    authInflux.setPrivileges(userName, userDB, Privileges.READ).get.value shouldEqual 200
  }

  it should "get user privileges" in {
    val userPrivs = authInflux.showUserPrivileges(userName).get.value

    userPrivs.length shouldEqual 1
    userPrivs.exists { upi =>
      upi.database == userDB && upi.privilege == Privileges.withName("READ")
    } shouldEqual true
  }

  it should "revoke user privileges" in {
    authInflux.revokePrivileges(userName, userDB, Privileges.READ).get.value shouldEqual 200
    authInflux.showUserPrivileges(userName).get.value shouldEqual Array(
      UserPrivilegesInfo(userDB, Privileges.NO_PRIVILEGES)
    )
  }

  it should "drop user" in {
    authInflux.dropUser(userName).get.value shouldEqual 200
    authInflux.dropUser(admin).get.value shouldEqual 200
  }
}
