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

package com.github.fsanaulla.chronicler.core.management

import com.github.fsanaulla.chronicler.core.alias.{ErrorOr, ResponseCode}
import com.github.fsanaulla.chronicler.core.components._
import com.github.fsanaulla.chronicler.core.enums.Privilege
import com.github.fsanaulla.chronicler.core.implicits._
import com.github.fsanaulla.chronicler.core.model._
import com.github.fsanaulla.chronicler.core.query.UserManagementQuery

trait UserManagement[F[_], G[_], Resp, Uri, Body] extends UserManagementQuery[Uri] {
  implicit val qb: QueryBuilder[Uri]
  implicit val re: RequestExecutor[F, Resp, Uri, Body]
  implicit val rh: ResponseHandler[G, Resp]
  implicit val F: Functor[F]
  implicit val FK: FunctionK[G, F]

  /** *
    * Create new username
    * @param username - Name for new user
    * @param password - Password for new user
    * @return         - Result of execution
    */
  final def createUser(username: String, password: String): F[ErrorOr[ResponseCode]] =
    F.flatMap(
      re.get(createUserQuery(username, password), compress = false)
    )(resp => FK(rh.writeResult(resp)))

  /** Create admin user
    * @param username - admin name
    * @param password - admin password
    * @return         - execution response
    */
  final def createAdmin(username: String, password: String): F[ErrorOr[ResponseCode]] =
    F.flatMap(
      re.get(createAdminQuery(username, password), compress = false)
    )(resp => FK(rh.writeResult(resp)))

  /** Drop user */
  final def dropUser(username: String): F[ErrorOr[ResponseCode]] =
    F.flatMap(
      re.get(dropUserQuery(username), compress = false)
    )(resp => FK(rh.writeResult(resp)))

  /** Set password for user */
  final def setUserPassword(username: String, password: String): F[ErrorOr[ResponseCode]] =
    F.flatMap(
      re.get(setUserPasswordQuery(username, password), compress = false)
    )(resp => FK(rh.writeResult(resp)))

  /** Set user privilege on specified database */
  final def setPrivileges(
      username: String,
      dbName: String,
      privilege: Privilege
  ): F[ErrorOr[ResponseCode]] =
    F.flatMap(
      re.get(setPrivilegesQuery(dbName, username, privilege), compress = false)
    )(resp => FK(rh.writeResult(resp)))

  /** Revoke user privilege on specified database */
  final def revokePrivileges(
      username: String,
      dbName: String,
      privilege: Privilege
  ): F[ErrorOr[ResponseCode]] =
    F.flatMap(
      re.get(revokePrivilegesQuery(dbName, username, privilege), compress = false)
    )(resp => FK(rh.writeResult(resp)))

  /** Grant admin rights */
  final def makeAdmin(username: String): F[ErrorOr[ResponseCode]] =
    F.flatMap(
      re.get(makeAdminQuery(username), compress = false)
    )(resp => FK(rh.writeResult(resp)))

  /** Remove admin rights */
  final def disableAdmin(username: String): F[ErrorOr[ResponseCode]] =
    F.flatMap(
      re.get(disableAdminQuery(username), compress = false)
    )(resp => FK(rh.writeResult(resp)))

  /** Show user lists */
  final def showUsers: F[ErrorOr[Array[UserInfo]]] =
    F.flatMap(
      re.get(showUsersQuery, compress = false)
    )(resp => FK(rh.queryResult[UserInfo](resp)))

  /** Show user privileges */
  final def showUserPrivileges(username: String): F[ErrorOr[Array[UserPrivilegesInfo]]] =
    F.flatMap(
      re.get(showUserPrivilegesQuery(username), compress = false)
    )(resp => FK(rh.queryResult[UserPrivilegesInfo](resp)))

}
