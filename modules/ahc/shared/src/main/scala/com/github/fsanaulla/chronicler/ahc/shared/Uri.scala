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

package com.github.fsanaulla.chronicler.ahc.shared

import java.net.URLEncoder

import org.asynchttpclient.Param

/** *
  * Syntetic container for request information
  *
  * @param schema - request schema
  * @param host   - request api address
  * @param port   - request port
  * @param query  - query path
  * @param params - query params
  *
  * @since 0.6.0
  */
final case class Uri(
    schema: String,
    host: String,
    port: Int,
    query: String,
    params: List[Param] = Nil
) {

  /** Append query parameter */
  def addParam(param: Param): Uri =
    this.copy(params = param :: params)

  /** Create string based representation */
  def mkUrl: String = {
    val encode: String => String =
      p => URLEncoder.encode(p, "UTF-8")

    val queryParams = params.reverse
      .map(p => p.getName + "=" + encode(p.getValue))
      .mkString("&")

    schema + "://" + host + ":" + port + query + "?" + queryParams
  }
}
