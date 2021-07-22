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

package com.github.fsanaulla.chronicler.core.query

import com.github.fsanaulla.chronicler.core.components.QueryBuilder
import com.github.fsanaulla.chronicler.core.enums.Privilege

private[fsanaulla] trait UserManagementQuery[U] {

  private[chronicler] final def showUsersQuery(implicit qb: QueryBuilder[U]): U =
    qb.buildQuery("/query", qb.appendCredentials("SHOW USERS"))

  private[chronicler] final def showUserPrivilegesQuery(
      username: String
  )(implicit qb: QueryBuilder[U]): U =
    qb.buildQuery("/query", qb.appendCredentials(s"SHOW GRANTS FOR $username"))

  private[chronicler] final def setUserPasswordQuery(
      username: String,
      password: String
  )(implicit qb: QueryBuilder[U]): U =
    qb.buildQuery("/query", qb.appendCredentials(s"SET PASSWORD FOR $username = '$password'"))

  private[chronicler] final def createAdminQuery(
      username: String,
      password: String
  )(implicit qb: QueryBuilder[U]): U =
    qb.buildQuery(
      "/query",
      qb.appendCredentials(s"CREATE USER $username WITH PASSWORD '$password' WITH ALL PRIVILEGES")
    )

  private[chronicler] final def makeAdminQuery(username: String)(implicit qb: QueryBuilder[U]): U =
    qb.buildQuery("/query", qb.appendCredentials(s"GRANT ALL PRIVILEGES TO $username"))

  private[chronicler] final def disableAdminQuery(
      username: String
  )(implicit qb: QueryBuilder[U]): U =
    qb.buildQuery("/query", qb.appendCredentials(s"REVOKE ALL PRIVILEGES FROM $username"))

  private[chronicler] final def createUserQuery(
      username: String,
      password: String
  )(implicit qb: QueryBuilder[U]): U =
    qb.buildQuery(
      "/query",
      qb.appendCredentials(s"CREATE USER $username WITH PASSWORD '$password'")
    )

  private[chronicler] final def dropUserQuery(username: String)(implicit qb: QueryBuilder[U]): U =
    qb.buildQuery("/query", qb.appendCredentials(s"DROP USER $username"))

  private[chronicler] final def setPrivilegesQuery(
      dbName: String,
      username: String,
      privileges: Privilege
  )(implicit qb: QueryBuilder[U]): U =
    qb.buildQuery("/query", qb.appendCredentials(s"GRANT $privileges ON $dbName TO $username"))

  private[chronicler] final def revokePrivilegesQuery(
      dbName: String,
      username: String,
      privileges: Privilege
  )(implicit qb: QueryBuilder[U]): U =
    qb.buildQuery("/query", qb.appendCredentials(s"REVOKE $privileges ON $dbName FROM $username"))

}
