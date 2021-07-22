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
import com.github.fsanaulla.chronicler.core.enums.{Consistency, Epoch, Precision}

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 27.08.17
  */
trait DatabaseOperationQuery[U] {

  private[this] def addPrettyQueryParam(
      pretty: Boolean,
      queryParams: List[(String, String)]
  ): List[(String, String)] =
    if (!pretty) queryParams
    else ("pretty" -> pretty.toString) :: queryParams

  private[this] def addEpochQueryParam(
      epoch: Epoch,
      queryParams: List[(String, String)]
  ): List[(String, String)] =
    if (epoch.isNone) queryParams
    else ("epoch" -> epoch.toString) :: queryParams

  // format: off
  private[chronicler] final def write(
      dbName: String,
      consistency: Consistency,
      precision: Precision,
      retentionPolicy: Option[String]
    )(implicit qb: QueryBuilder[U]): U = {
    val queryParams = Nil

    val withRP =
      if (retentionPolicy.isEmpty) queryParams
      else "rp" -> retentionPolicy.get :: queryParams

    val withPrecision =
      if (precision.isNone) withRP
      else "precision" -> precision.toString :: withRP

    val withConsistency =
      if (consistency.isNone) withPrecision
      else "consistency" -> consistency.toString :: withPrecision

    qb.buildQuery("/write", qb.appendCredentials(dbName, withConsistency))
  }

  private[chronicler] final def singleQuery(
      dbName: String,
      query: String,
      epoch: Epoch,
      pretty: Boolean
    )(implicit qb: QueryBuilder[U]): U = {
    val queryParams = List("q" -> query)
    val withEpoch   = addEpochQueryParam(epoch, queryParams)
    val withPretty  = addPrettyQueryParam(pretty, withEpoch)

    qb.buildQuery("/query", qb.appendCredentials(dbName, withPretty))
  }

  private[chronicler] final def chunkedQuery(
      dbName: String,
      query: String,
      epoch: Epoch,
      pretty: Boolean,
      chunkSize: Int
    )(implicit qb: QueryBuilder[U]): U = {

    val queryParams = List(
      "db"         -> dbName,
      "q"          -> query,
      "chunked"    -> String.valueOf(true),
      "chunk_size" -> String.valueOf(chunkSize)
    )

    val withEpoch  = addEpochQueryParam(epoch, queryParams)
    val withPretty = addPrettyQueryParam(pretty, withEpoch)

    qb.buildQuery("/query", qb.appendCredentials(withPretty))
  }

  private[chronicler] final def bulkQuery(
      dbName: String,
      queries: Seq[String],
      epoch: Epoch,
      pretty: Boolean
    )(implicit qb: QueryBuilder[U]): U = {
    val queryParams = List("q" -> queries.mkString(";"))
    val withEpoch   = addEpochQueryParam(epoch, queryParams)
    val withPretty  = addPrettyQueryParam(pretty, withEpoch)

    qb.buildQuery("/query", qb.appendCredentials(dbName, withPretty))
  }
}
