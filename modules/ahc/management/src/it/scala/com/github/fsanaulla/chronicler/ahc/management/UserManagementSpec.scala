package com.github.fsanaulla.chronicler.ahc.management

import com.github.fsanaulla.chronicler.core.enums.Privileges
import com.github.fsanaulla.chronicler.core.model.{UserInfo, UserPrivilegesInfo}
import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 10.08.17
  */
class UserManagementSpec
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

  val userDB    = "db"
  val userName  = "Martin"
  val userPass  = "pass"
  val userNPass = "new_pass"

  val admin     = "Admin"
  val adminPass = "admin_pass"

  lazy val influx: AhcManagementClient =
    InfluxMng(host, port, Some(creds))

  "User Management API" should "create user" in {
    influx.createDatabase(userDB).futureValue.value shouldEqual 200

    influx.createUser(userName, userPass).futureValue.value shouldEqual 200
    influx.showUsers.futureValue.value
      .contains(UserInfo(userName, isAdmin = false)) shouldEqual true
  }

  it should "create admin" in {
    influx.createAdmin(admin, adminPass).futureValue.value shouldEqual 200
    influx.showUsers.futureValue.value
      .contains(UserInfo(admin, isAdmin = true)) shouldEqual true
  }

  it should "show user privileges" in {
    influx.showUserPrivileges(admin).futureValue.value shouldEqual Nil
  }

  it should "set user password" in {
    influx.setUserPassword(userName, userNPass).futureValue.value shouldEqual 200
  }

  it should "set privileges" in {
    influx.setPrivileges(userName, userDB, Privileges.READ).futureValue.value shouldEqual 200
    influx
      .setPrivileges("unknown", userDB, Privileges.READ)
      .futureValue
      .left
      .value
      .getMessage shouldEqual "user not found"

    influx.showUserPrivileges(userName).futureValue.value shouldEqual Array(
      UserPrivilegesInfo(userDB, Privileges.READ)
    )
  }

  it should "revoke privileges" in {
    influx.revokePrivileges(userName, userDB, Privileges.READ).futureValue.value shouldEqual 200
    influx.showUserPrivileges(userName).futureValue.value shouldEqual Array(
      UserPrivilegesInfo(userDB, Privileges.NO_PRIVILEGES)
    )
  }

  it should "disable admin" in {
    influx.disableAdmin(admin).futureValue.value shouldEqual 200
    influx.showUsers.futureValue.value
      .contains(UserInfo(admin, isAdmin = false)) shouldEqual true
  }

  it should "make admin" in {
    influx.makeAdmin(admin).futureValue.value shouldEqual 200
    influx.showUsers.futureValue.value
      .contains(UserInfo(admin, isAdmin = true)) shouldEqual true
  }

  it should "drop users" in {
    influx.dropUser(userName).futureValue.value shouldEqual 200
    influx.dropUser(admin).futureValue.value shouldEqual 200
  }
}
