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
import com.github.fsanaulla.chronicler.core.implicits._
import com.github.fsanaulla.chronicler.core.model._
import com.github.fsanaulla.chronicler.core.query.DataManagementQuery

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 08.08.17
  */
trait DatabaseManagement[F[_], G[_], Resp, Uri, Body] extends DataManagementQuery[Uri] {
  implicit val qb: QueryBuilder[Uri]
  implicit val re: RequestExecutor[F, Resp, Uri, Body]
  implicit val rh: ResponseHandler[G, Resp]
  implicit val F: Functor[F]
  implicit val FK: FunctionK[G, F]

  /** Create database
    *
    * @param dbName        - database name
    * @param duration      - database duration
    * @param replication   - replication
    * @param shardDuration - shard duration
    * @param rpName        - retention policy name
    * @return              - execution R
    */
  final def createDatabase(
      dbName: String,
      duration: Option[String] = None,
      replication: Option[Int] = None,
      shardDuration: Option[String] = None,
      rpName: Option[String] = None
  ): F[ErrorOr[ResponseCode]] =
    F.flatMap(
      re.post(
        createDatabaseQuery(dbName, duration, replication, shardDuration, rpName)
      )
    )(resp => FK(rh.writeResult(resp)))

  /** Drop database */
  final def dropDatabase(dbName: String): F[ErrorOr[ResponseCode]] =
    F.flatMap(
      re.get(dropDatabaseQuery(dbName), compress = false)
    )(resp => FK(rh.writeResult(resp)))

  /** Drop measurement */
  final def dropMeasurement(dbName: String, measurementName: String): F[ErrorOr[ResponseCode]] =
    F.flatMap(
      re.get(dropMeasurementQuery(dbName, measurementName), compress = false)
    )(resp => FK(rh.writeResult(resp)))

  /** Show measurements */
  final def showMeasurement(
      dbName: String,
      compressed: Boolean = false
  ): F[ErrorOr[Array[String]]] =
    F.flatMap(
      re.get(showMeasurementQuery(dbName), compressed)
    )(resp => FK(rh.queryResult[String](resp)))

  /** Show database list */
  final def showDatabases(compressed: Boolean = false): F[ErrorOr[Array[String]]] =
    F.flatMap(
      re.get(showDatabasesQuery, compressed)
    )(resp => FK(rh.queryResult[String](resp)))

  /** Show field tags list */
  final def showFieldKeys(
      dbName: String,
      measurementName: String,
      compressed: Boolean = false
  ): F[ErrorOr[Array[FieldInfo]]] =
    F.flatMap(
      re.get(showFieldKeysQuery(dbName, measurementName), compressed)
    )(resp => FK(rh.queryResult[FieldInfo](resp)))

  /** Show tags keys list */
  final def showTagKeys(
      dbName: String,
      measurementName: String,
      whereClause: Option[String] = None,
      limit: Option[Int] = None,
      offset: Option[Int] = None,
      compressed: Boolean = false
  ): F[ErrorOr[Array[String]]] =
    F.flatMap(
      re.get(showTagKeysQuery(dbName, measurementName, whereClause, limit, offset), compressed)
    )(resp => FK(rh.queryResult[String](resp)))

  /** Show tag values list */
  final def showTagValues(
      dbName: String,
      measurementName: String,
      withKey: Seq[String],
      whereClause: Option[String] = None,
      limit: Option[Int] = None,
      offset: Option[Int] = None,
      compressed: Boolean = false
  ): F[ErrorOr[Array[TagValue]]] =
    F.flatMap(
      re.get(
        showTagValuesQuery(dbName, measurementName, withKey, whereClause, limit, offset),
        compressed
      )
    )(resp => FK(rh.queryResult[TagValue](resp)))
}
