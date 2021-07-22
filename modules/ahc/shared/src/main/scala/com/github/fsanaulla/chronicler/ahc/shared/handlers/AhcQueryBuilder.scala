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

package com.github.fsanaulla.chronicler.ahc.shared.handlers

import com.github.fsanaulla.chronicler.ahc.shared.Uri
import com.github.fsanaulla.chronicler.core.components.QueryBuilder
import com.github.fsanaulla.chronicler.core.model.InfluxCredentials
import org.asynchttpclient.Param

import scala.annotation.tailrec

private[ahc] class AhcQueryBuilder(
    schema: String,
    host: String,
    port: Int,
    credentials: Option[InfluxCredentials]
) extends QueryBuilder[Uri](credentials) {

  override def buildQuery(query: String): Uri =
    Uri(schema, host, port, query)

  override def buildQuery(query: String, queryParams: List[(String, String)]): Uri = {
    val u      = buildQuery(query)
    val params = queryParams.map { case (k, v) => new Param(k, v) }

    @tailrec
    def addQueryParam(u: Uri, lst: List[Param]): Uri = {
      lst match {
        case Nil       => u
        case h :: tail => addQueryParam(u.addParam(h), tail)
      }
    }

    addQueryParam(u, params)
  }
}
