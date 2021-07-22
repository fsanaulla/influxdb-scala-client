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

package com.github.fsanaulla.chronicler.akka.shared.handlers

import akka.http.scaladsl.model.Uri
import com.github.fsanaulla.chronicler.core.components.QueryBuilder
import com.github.fsanaulla.chronicler.core.model.InfluxCredentials

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 15.03.18
  */
private[akka] class AkkaQueryBuilder(
    schema: String,
    host: String,
    port: Int,
    credentials: Option[InfluxCredentials]
) extends QueryBuilder[Uri](credentials) {

  override def buildQuery(url: String): Uri =
    Uri.from(
      schema,
      host = host,
      port = port,
      path = url
    )

  override def buildQuery(url: String, queryParams: List[(String, String)]): Uri =
    buildQuery(url).withQuery(Uri.Query(queryParams: _*))
}
