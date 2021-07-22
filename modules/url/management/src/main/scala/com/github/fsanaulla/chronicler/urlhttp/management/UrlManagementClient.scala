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

package com.github.fsanaulla.chronicler.urlhttp.management

import com.github.fsanaulla.chronicler.core.ManagementClient
import com.github.fsanaulla.chronicler.core.alias.{ErrorOr, Id}
import com.github.fsanaulla.chronicler.core.components.ResponseHandler
import com.github.fsanaulla.chronicler.core.implicits.{applyId, functorId}
import com.github.fsanaulla.chronicler.core.model.{
  FunctionK,
  Functor,
  InfluxCredentials,
  InfluxDBInfo
}
import com.github.fsanaulla.chronicler.urlhttp.shared.Url
import com.github.fsanaulla.chronicler.urlhttp.shared.handlers.{
  UrlJsonHandler,
  UrlQueryBuilder,
  UrlRequestExecutor
}
import requests.Response

import scala.util.Try

final class UrlManagementClient(
    host: String,
    port: Int,
    credentials: Option[InfluxCredentials]
)(implicit val F: Functor[Try], val FK: FunctionK[Id, Try])
    extends ManagementClient[Try, Id, Response, Url, String] {

  val jsonHandler                                = new UrlJsonHandler(compressed = false)
  implicit val qb: UrlQueryBuilder               = new UrlQueryBuilder(host, port, credentials)
  implicit val re: UrlRequestExecutor            = new UrlRequestExecutor(jsonHandler)
  implicit val rh: ResponseHandler[Id, Response] = new ResponseHandler(jsonHandler)

  override def ping: Try[ErrorOr[InfluxDBInfo]] = {
    re.get(qb.buildQuery("/ping"), compress = false)
      .map(rh.pingResult)
  }

  override def close(): Unit = {}
}
