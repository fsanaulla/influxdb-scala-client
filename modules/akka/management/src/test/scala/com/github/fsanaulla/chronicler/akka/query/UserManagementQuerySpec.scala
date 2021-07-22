/*
 * Copyright 2017-2019 Faiaz Sanaulla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fsanaulla.chronicler.akka.query

import akka.http.scaladsl.model.Uri
import com.github.fsanaulla.chronicler.akka.shared.handlers.AkkaQueryBuilder
import com.github.fsanaulla.chronicler.core.enums.Privileges
import com.github.fsanaulla.chronicler.core.model.InfluxCredentials
import com.github.fsanaulla.chronicler.core.query.UserManagementQuery
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 21.08.17
  */
class UserManagementQuerySpec extends AnyFlatSpec with Matchers with UserManagementQuery[Uri] {

  trait AuthEnv {
    val credentials: Option[InfluxCredentials] = Some(InfluxCredentials("admin", "admin"))
    implicit val qb: AkkaQueryBuilder          = new AkkaQueryBuilder("http", "localhost", 8086, credentials)
  }

  trait NonAuthEnv {
    implicit val qb: AkkaQueryBuilder = new AkkaQueryBuilder("http", "localhost", 8086, None)
  }

  private val testUsername  = "TEST_USER_NAME"
  private val testPassword  = "TEST_PASSWORD"
  private val testDatabase  = "TEST_DATABASE"
  private val testPrivilege = Privileges.ALL

  "UserManagementQuery" should "create user query" in new AuthEnv {
    createUserQuery(testUsername, testPassword) shouldEqual
      queryTesterAuth(s"CREATE USER $testUsername WITH PASSWORD '$testPassword'")(credentials.get)
  }

  it should "create user query without auth" in new NonAuthEnv {
    createUserQuery(testUsername, testPassword) shouldEqual
      queryTester(s"CREATE USER $testUsername WITH PASSWORD '$testPassword'")
  }

  it should "create admin user query" in new AuthEnv {
    createAdminQuery(testUsername, testPassword) shouldEqual
      queryTesterAuth(
        s"CREATE USER $testUsername WITH PASSWORD '$testPassword' WITH ALL PRIVILEGES"
      )(credentials.get)

  }

  it should "create admin user query without auth" in new NonAuthEnv {
    createAdminQuery(testUsername, testPassword) shouldEqual
      queryTester(s"CREATE USER $testUsername WITH PASSWORD '$testPassword' WITH ALL PRIVILEGES")
  }

  it should "drop user query" in new AuthEnv {
    dropUserQuery(testUsername) shouldEqual
      queryTesterAuth(s"DROP USER $testUsername")(credentials.get)
  }

  it should "drop user query without auth" in new NonAuthEnv {
    dropUserQuery(testUsername) shouldEqual
      queryTester(s"DROP USER $testUsername")
  }

  it should "show users query" in new AuthEnv {
    showUsersQuery shouldEqual queryTesterAuth("SHOW USERS")(credentials.get)
  }

  it should "show users query without auth" in new NonAuthEnv {
    showUsersQuery shouldEqual queryTester("SHOW USERS")
  }

  it should "show user privileges query" in new AuthEnv {
    showUserPrivilegesQuery(testUsername) shouldEqual
      queryTesterAuth(s"SHOW GRANTS FOR $testUsername")(credentials.get)
  }

  it should "show user privileges query without auth" in new NonAuthEnv {
    showUserPrivilegesQuery(testUsername) shouldEqual queryTester(
      s"SHOW GRANTS FOR $testUsername"
    )
  }

  it should "make admin query" in new AuthEnv {
    makeAdminQuery(testUsername) shouldEqual
      queryTesterAuth(s"GRANT ALL PRIVILEGES TO $testUsername")(credentials.get)
  }

  it should "make admin query without auth" in new NonAuthEnv {
    makeAdminQuery(testUsername) shouldEqual queryTester(
      s"GRANT ALL PRIVILEGES TO $testUsername"
    )
  }

  it should "disable admin query" in new AuthEnv {
    disableAdminQuery(testUsername) shouldEqual
      queryTesterAuth(s"REVOKE ALL PRIVILEGES FROM $testUsername")(credentials.get)
  }

  it should "disable admin query without auth" in new NonAuthEnv {
    disableAdminQuery(testUsername) shouldEqual queryTester(
      s"REVOKE ALL PRIVILEGES FROM $testUsername"
    )
  }

  it should "set user password query" in new AuthEnv {
    setUserPasswordQuery(testUsername, testPassword) shouldEqual
      queryTesterAuth(s"SET PASSWORD FOR $testUsername = '$testPassword'")(credentials.get)
  }

  it should "set user password query without auth" in new NonAuthEnv {
    setUserPasswordQuery(testUsername, testPassword) shouldEqual
      queryTester(s"SET PASSWORD FOR $testUsername = '$testPassword'")
  }

  it should "set privileges query" in new AuthEnv {
    setPrivilegesQuery(testDatabase, testUsername, testPrivilege) shouldEqual
      queryTesterAuth(s"GRANT $testPrivilege ON $testDatabase TO $testUsername")(credentials.get)
  }

  it should "set privileges query without auth" in new NonAuthEnv {
    setPrivilegesQuery(testDatabase, testUsername, testPrivilege) shouldEqual
      queryTester(s"GRANT $testPrivilege ON $testDatabase TO $testUsername")
  }

  it should "revoke privileges query" in new AuthEnv {
    revokePrivilegesQuery(testDatabase, testUsername, testPrivilege) shouldEqual
      queryTesterAuth(s"REVOKE $testPrivilege ON $testDatabase FROM $testUsername")(credentials.get)
  }

  it should "revoke privileges query without auth" in new NonAuthEnv {
    revokePrivilegesQuery(testDatabase, testUsername, testPrivilege) shouldEqual
      queryTester(s"REVOKE $testPrivilege ON $testDatabase FROM $testUsername")
  }
}
