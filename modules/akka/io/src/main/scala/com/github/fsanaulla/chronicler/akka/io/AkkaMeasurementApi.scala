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

package com.github.fsanaulla.chronicler.akka.io

import akka.http.scaladsl.model.{HttpResponse, RequestEntity, Uri}
import akka.stream.scaladsl.Source
import com.github.fsanaulla.chronicler.akka.shared.handlers.{
  AkkaQueryBuilder,
  AkkaRequestExecutor,
  AkkaResponseHandler
}
import com.github.fsanaulla.chronicler.core.alias.ErrorOr
import com.github.fsanaulla.chronicler.core.api.MeasurementApi
import com.github.fsanaulla.chronicler.core.components.BodyBuilder
import com.github.fsanaulla.chronicler.core.enums.{Epoch, Epochs}
import com.github.fsanaulla.chronicler.core.model.{Failable, Functor, InfluxReader}

import scala.concurrent.Future
import scala.reflect.ClassTag

final class AkkaMeasurementApi[T: ClassTag](
    dbName: String,
    measurementName: String,
    gzipped: Boolean
)(implicit
    qb: AkkaQueryBuilder,
    bd: BodyBuilder[RequestEntity],
    re: AkkaRequestExecutor,
    rh: AkkaResponseHandler,
    F: Functor[Future],
    FA: Failable[Future]
) extends MeasurementApi[Future, Future, HttpResponse, Uri, RequestEntity, T](
      dbName,
      measurementName,
      gzipped
    ) {

  /** Read chunked data, typed
    *
    * @param query     - request SQL query
    * @param epoch     - epoch precision
    * @param pretty    - pretty printed result
    * @param chunkSize - number of elements in the response chunk
    * @return          - streaming response of batched items
    * @since 0.5.4
    */
  def readChunked(
      query: String,
      epoch: Epoch = Epochs.None,
      pretty: Boolean = false,
      chunkSize: Int
  )(implicit rd: InfluxReader[T]): Future[Source[ErrorOr[Array[T]], Any]] = {
    val uri = chunkedQuery(dbName, query, epoch, pretty, chunkSize)
    F.map(re.get(uri, compressed = false))(rh.queryChunkedResult[T])
  }
}
