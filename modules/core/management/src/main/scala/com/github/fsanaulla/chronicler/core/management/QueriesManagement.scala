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
import com.github.fsanaulla.chronicler.core.query.QueriesManagementQuery

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 19.08.17
  */
trait QueriesManagement[F[_], G[_], Resp, Uri, Entity] extends QueriesManagementQuery[Uri] {
  implicit val qb: QueryBuilder[Uri]
  implicit val re: RequestExecutor[F, Resp, Uri, Entity]
  implicit val rh: ResponseHandler[G, Resp]
  implicit val F: Functor[F]
  implicit val FK: FunctionK[G, F]

  /** Show list of queries */
  final def showQueries: F[ErrorOr[Array[QueryInfo]]] =
    F.flatMap(
      re.get(showQuerysQuery, compress = false)
    )(resp => FK(rh.queryResult[QueryInfo](resp)))

  /** Kill query */
  final def killQuery(queryId: Int): F[ErrorOr[ResponseCode]] =
    F.flatMap(
      re.get(killQueryQuery(queryId), compress = false)
    )(resp => FK(rh.writeResult(resp)))
}
