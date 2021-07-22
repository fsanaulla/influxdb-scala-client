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

import com.github.fsanaulla.chronicler.core.alias.{ErrorOr, Id, JPoint}
import com.github.fsanaulla.chronicler.core.api.DatabaseApi
import com.github.fsanaulla.chronicler.core.components.{BodyBuilder, ResponseHandler}
import com.github.fsanaulla.chronicler.core.enums.{Epoch, Epochs}
import com.github.fsanaulla.chronicler.core.model.{FunctionK, Functor}
import com.github.fsanaulla.chronicler.urlhttp.shared.Url
import com.github.fsanaulla.chronicler.urlhttp.shared.handlers.{UrlQueryBuilder, UrlRequestExecutor}
import requests.Response

import scala.util.Try

final class UrlDatabaseApi(
    dbName: String,
    compress: Boolean
)(implicit
    qb: UrlQueryBuilder,
    bd: BodyBuilder[String],
    re: UrlRequestExecutor,
    rh: ResponseHandler[Id, Response],
    F: Functor[Try],
    FK: FunctionK[Id, Try]
) extends DatabaseApi[Try, Id, Response, Url, String](dbName, compress) {

  /** Chunked query execution with json response
    *
    * @param query     - influx compatible SQL query
    * @param epoch     - epoch timestamp precision
    * @param pretty    - pretty printing response
    * @param chunkSize - count points in the chunk
    * @return          - chunks iterator
    *
    * @since           - 0.5.2
    */
  def readChunkedJson(
      query: String,
      epoch: Epoch = Epochs.None,
      pretty: Boolean = false,
      chunkSize: Int = 10000
  ): Try[Iterator[ErrorOr[Array[JPoint]]]] = {
    val uri = chunkedQuery(dbName, query, epoch, pretty, chunkSize)
    re.getStream(uri)
  }
}
