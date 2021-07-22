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

package com.github.fsanaulla.chronicler.core.api

import java.nio.file.Path

import com.github.fsanaulla.chronicler.core.alias.{ErrorOr, ResponseCode, Tags, Values}
import com.github.fsanaulla.chronicler.core.components._
import com.github.fsanaulla.chronicler.core.enums._
import com.github.fsanaulla.chronicler.core.model._
import com.github.fsanaulla.chronicler.core.query.DatabaseOperationQuery
import org.typelevel.jawn.ast.JArray

/** Generic interface for basic database IO operation
  *
  * @tparam F - request execution effect
  * @tparam G - response parsing effect
  * @tparam R - HTTP response
  * @tparam U - HTTP URi
  * @tparam E - Entity
  *
  * @since Big Bang
  */
class DatabaseApi[F[_], G[_], R, U, E](
    dbName: String,
    compress: Boolean
)(implicit
    qb: QueryBuilder[U],
    bd: BodyBuilder[E],
    re: RequestExecutor[F, R, U, E],
    rh: ResponseHandler[G, R],
    F: Functor[F],
    FK: FunctionK[G, F]
) extends DatabaseOperationQuery[U] {

  def writeFromFile(
      filePath: Path,
      enc: String = "UTF-8",
      consistency: Consistency = Consistencies.None,
      precision: Precision = Precisions.None,
      retentionPolicy: Option[String] = None
  ): F[ErrorOr[ResponseCode]] = {
    val uri = write(dbName, consistency, precision, retentionPolicy)
    F.flatMap(
      re.post(uri, bd.fromFile(filePath, enc), compress)
    )(resp => FK(rh.writeResult(resp)))
  }

  def writeNative(
      point: String,
      consistency: Consistency = Consistencies.None,
      precision: Precision = Precisions.None,
      retentionPolicy: Option[String] = None
  ): F[ErrorOr[ResponseCode]] = {
    val uri = write(dbName, consistency, precision, retentionPolicy)
    F.flatMap(
      re.post(uri, bd.fromString(point), compress)
    )(resp => FK(rh.writeResult(resp)))
  }

  def bulkWriteNative(
      points: Seq[String],
      consistency: Consistency = Consistencies.None,
      precision: Precision = Precisions.None,
      retentionPolicy: Option[String] = None
  ): F[ErrorOr[ResponseCode]] = {
    val uri = write(dbName, consistency, precision, retentionPolicy)
    F.flatMap(
      re.post(uri, bd.fromStrings(points), compress)
    )(resp => FK(rh.writeResult(resp)))
  }

  def writePoint(
      point: Point,
      consistency: Consistency = Consistencies.None,
      precision: Precision = Precisions.None,
      retentionPolicy: Option[String] = None
  ): F[ErrorOr[ResponseCode]] = {
    val uri = write(dbName, consistency, precision, retentionPolicy)
    F.flatMap(
      re.post(uri, bd.fromPoint(point), compress)
    )(resp => FK(rh.writeResult(resp)))
  }

  def bulkWritePoints(
      points: Seq[Point],
      consistency: Consistency = Consistencies.None,
      precision: Precision = Precisions.None,
      retentionPolicy: Option[String] = None
  ): F[Either[Throwable, ResponseCode]] = {
    val uri = write(dbName, consistency, precision, retentionPolicy)
    F.flatMap(
      re.post(uri, bd.fromPoints(points), compress)
    )(resp => FK(rh.writeResult(resp)))
  }

  def readJson(
      query: String,
      epoch: Epoch = Epochs.None,
      pretty: Boolean = false
  ): F[ErrorOr[Array[JArray]]] = {
    val uri = singleQuery(dbName, query, epoch, pretty)
    F.flatMap(re.get(uri, compress))(resp => FK(rh.queryResultJson(resp)))
  }

  def bulkReadJson(
      queries: Seq[String],
      epoch: Epoch = Epochs.None,
      pretty: Boolean = false
  ): F[ErrorOr[Array[Array[JArray]]]] = {
    val uri = bulkQuery(dbName, queries, epoch, pretty)
    F.flatMap(re.get(uri, compress))(resp => FK(rh.bulkQueryResultJson(resp)))
  }

  def readGroupedJson(
      query: String,
      epoch: Epoch = Epochs.None,
      pretty: Boolean = false
  ): F[ErrorOr[Array[(Tags, Values)]]] = {
    val uri = singleQuery(dbName, query, epoch, pretty)
    F.flatMap(
      re.get(uri, compress)
    )(resp => FK(rh.groupedResultJson(resp)))
  }
}
