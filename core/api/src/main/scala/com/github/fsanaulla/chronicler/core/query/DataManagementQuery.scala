/*
 * Copyright 2017-2018 Faiaz Sanaulla
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

import com.github.fsanaulla.chronicler.core.model.InfluxCredentials
import com.github.fsanaulla.chronicler.core.typeclasses.QueryBuilder

import scala.collection.mutable

/**
  * Created by fayaz on 27.06.17.
  */
private[chronicler] trait DataManagementQuery[U] { self: QueryBuilder[U] =>

  final def createDatabaseQuery(dbName: String,
                                duration: Option[String],
                                replication: Option[Int],
                                shardDuration: Option[String],
                                rpName: Option[String])
                               (implicit credentials: Option[InfluxCredentials]): U = {

    val sb = StringBuilder.newBuilder

    sb.append(s"CREATE DATABASE $dbName")

    if (duration.isDefined || replication.isDefined || shardDuration.isDefined || rpName.isDefined) {
      sb.append(" WITH")
    }

    for (d <- duration) {
      sb.append(s" DURATION $d")
    }

    for (r <- replication) {
      sb.append(s" REPLICATION $r")
    }

    for (sd <- shardDuration) {
      sb.append(s" SHARD DURATION $sd")
    }

    for (rp <- rpName) {
      sb.append(s" NAME $rp")
    }

    buildQuery("/query", buildQueryParams(sb.toString()))
  }

  final def dropDatabaseQuery(dbName: String)
                             (implicit credentials: Option[InfluxCredentials]): U =
    buildQuery("/query", buildQueryParams(s"DROP DATABASE $dbName"))

  final def dropSeriesQuery(dbName: String, seriesName: String)
                           (implicit credentials: Option[InfluxCredentials]): U =
    buildQuery("/query", buildQueryParams(mutable.Map("db" -> dbName, "q" -> s"DROP SERIES FROM $seriesName")))

  final def dropMeasurementQuery(dbName: String, measurementName: String)
                                (implicit credentials: Option[InfluxCredentials]): U =
    buildQuery("/query", buildQueryParams(mutable.Map("db" -> dbName, "q" -> s"DROP MEASUREMENT $measurementName")))

  final def deleteAllSeriesQuery(dbName: String, seriesName: String)
                                (implicit credentials: Option[InfluxCredentials]): U =
    buildQuery("/query", buildQueryParams(mutable.Map("db" -> dbName, "q" -> s"DELETE FROM $seriesName")))

  final def showMeasurementQuery(dbName: String)
                                (implicit credentials: Option[InfluxCredentials]): U =
    buildQuery("/query", buildQueryParams(mutable.Map("db" -> dbName, "q" -> s"SHOW MEASUREMENTS")))

  final def showDatabasesQuery(implicit credentials: Option[InfluxCredentials]): U =
    buildQuery("/query", buildQueryParams(s"SHOW DATABASES"))

  final def showFieldKeysQuery(dbName: String, measurementName: String)(implicit credentials: Option[InfluxCredentials]): U =
    buildQuery("/query", buildQueryParams(s"SHOW FIELD KEYS ON $dbName FROM $measurementName"))

  final def showTagKeysQuery(dbName: String,
                             measurementName: String,
                             whereClause: Option[String],
                             limit: Option[Int],
                             offset: Option[Int])(implicit credentials: Option[InfluxCredentials]): U = {
    val sb = StringBuilder.newBuilder

    sb.append("SHOW TAG KEYS ON ")
      .append(dbName)
      .append(" FROM ")
      .append(measurementName)

    for (where <- whereClause) {
      sb.append(" WHERE ").append(where)
    }

    for (l <- limit) {
      sb.append(" LIMIT ").append(l)
    }

    for (o <- offset) {
      sb.append(" OFFSET ").append(o)
    }

    buildQuery("/query", buildQueryParams(sb.toString()))
  }

  final def showTagValuesQuery(dbName: String,
                               measurementName: String,
                               withKey: Seq[String],
                               whereClause: Option[String],
                               limit: Option[Int],
                               offset: Option[Int])(implicit credentials: Option[InfluxCredentials]): U = {
    require(withKey.nonEmpty, "Keys can't be empty")

    val sb = StringBuilder.newBuilder

    sb.append("SHOW TAG VALUES ON ")
      .append(dbName)
      .append(" FROM ")
      .append(measurementName)

    if (withKey.lengthCompare(1) == 0) {
      sb.append(" WITH KEY = ").append(withKey.head)
    } else {
      sb.append(" WITH KEY IN ").append(s"(${withKey.mkString(",")})")
    }

    for (where <- whereClause) {
      sb.append(" WHERE ").append(where)
    }

    for (l <- limit) {
      sb.append(" LIMIT ").append(l)
    }

    for (o <- offset) {
      sb.append(" OFFSET ").append(o)
    }

    buildQuery("/query", buildQueryParams(sb.toString()))
  }
}
