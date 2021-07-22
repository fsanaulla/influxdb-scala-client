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

private[chronicler] trait RetentionPolicyManagementQuery[U] {

  private[chronicler] final def createRPQuery(
      rpName: String,
      dbName: String,
      duration: String,
      replication: Int,
      shardDuration: Option[String],
      default: Boolean = false
  )(implicit qb: QueryBuilder[U]): U = {
    val sb = new StringBuilder()

    sb.append("CREATE RETENTION POLICY ")
      .append(rpName)
      .append(" ON ")
      .append(dbName)
      .append(" DURATION ")
      .append(duration)
      .append(" REPLICATION ")
      .append(replication)

    for (sd <- shardDuration) {
      sb.append(" SHARD DURATION ").append(sd)
    }

    if (default) sb.append(" DEFAULT")

    qb.buildQuery("/query", qb.appendCredentials(sb.toString()))
  }

  private[chronicler] final def dropRPQuery(
      rpName: String,
      dbName: String
  )(implicit qb: QueryBuilder[U]): U =
    qb.buildQuery("/query", qb.appendCredentials(s"DROP RETENTION POLICY $rpName ON $dbName"))

  private[chronicler] final def updateRPQuery(
      rpName: String,
      dbName: String,
      duration: Option[String],
      replication: Option[Int],
      shardDuration: Option[String],
      default: Boolean = false
  )(implicit qb: QueryBuilder[U]): U = {
    val sb = new StringBuilder()

    sb.append("ALTER RETENTION POLICY ")
      .append(rpName)
      .append(" ON ")
      .append(dbName)

    for (d <- duration) {
      sb.append(" DURATION ").append(d)
    }

    for (r <- replication) {
      sb.append(" REPLICATION ").append(r)
    }

    for (sd <- shardDuration) {
      sb.append(" SHARD DURATION ").append(sd)
    }

    if (default) sb.append(" DEFAULT")

    qb.buildQuery("/query", qb.appendCredentials(sb.toString()))
  }

  private[chronicler] final def showRPQuery(dbName: String)(implicit qb: QueryBuilder[U]): U =
    qb.buildQuery(
      "/query",
      qb.appendCredentials(List("db" -> dbName, "q" -> "SHOW RETENTION POLICIES"))
    )
}
