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

package com.github.fsanaulla.chronicler.urlhttp.io

import com.github.fsanaulla.chronicler.core.alias.{ErrorOr, Id}
import com.github.fsanaulla.chronicler.core.api.MeasurementApi
import com.github.fsanaulla.chronicler.core.components.{BodyBuilder, ResponseHandler}
import com.github.fsanaulla.chronicler.core.either
import com.github.fsanaulla.chronicler.core.either._
import com.github.fsanaulla.chronicler.core.enums.{Epoch, Epochs}
import com.github.fsanaulla.chronicler.core.model.{Failable, FunctionK, Functor, InfluxReader}
import com.github.fsanaulla.chronicler.urlhttp.shared.Url
import com.github.fsanaulla.chronicler.urlhttp.shared.handlers.{UrlQueryBuilder, UrlRequestExecutor}
import requests.Response

import scala.reflect.ClassTag
import scala.util.Try

class UrlMeasurementApi[T: ClassTag](
    dbName: String,
    measurementName: String,
    gzipped: Boolean
)(implicit
    qb: UrlQueryBuilder,
    bd: BodyBuilder[String],
    re: UrlRequestExecutor,
    rh: ResponseHandler[Id, Response],
    F: Functor[Try],
    FA: Failable[Try],
    FK: FunctionK[Id, Try]
) extends MeasurementApi[Try, Id, Response, Url, String, T](dbName, measurementName, gzipped) {

  /** Chunked query execution with typed response
    *
    * @param query     - influx compatible SQL query
    * @param epoch     - epoch timestamp precision
    * @param pretty    - pretty printing response
    * @param chunkSize - count points in the chunk
    * @param rd        - reader
    * @return          - iterator with chunked response
    * @since           - 0.5.2
    */
  def readChunked(
      query: String,
      epoch: Epoch = Epochs.None,
      pretty: Boolean = false,
      chunkSize: Int = 10000
  )(implicit rd: InfluxReader[T]): Try[Iterator[ErrorOr[Array[T]]]] = {
    val uri = chunkedQuery(dbName, query, epoch, pretty, chunkSize)
    re.getStream(uri)
      .map(_.map(_.flatMapRight(arr => either.array[Throwable, T](arr.map(rd.read)))))
  }
}
