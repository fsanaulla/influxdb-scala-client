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

package com.github.fsanaulla.chronicler.core.components

import com.github.fsanaulla.chronicler.core.model.InfluxCredentials

/** Trait that define functionality for handling query building
  *
  * @tparam U - Result type parameter, for example for AkkaHttpBackend
  *           - used `akka.http.scaladsl.model.Uri`
  */
abstract class QueryBuilder[U](credentials: Option[InfluxCredentials]) {

  def buildQuery(url: String): U

  /** Method that build result URI object of type [A], from uri path, and query parameters
    *
    * @param uri         - string based uri path
    * @param queryParams - query parameters that will be embedded into request
    * @return            - URI object
    */
  def buildQuery(uri: String, queryParams: List[(String, String)]): U

  /** Method that embed credentials to already created query parameters map, sorted by key
    *
    * @param queryMap - query parameters map
    * @return         - updated query parameters map with embedded credentials
    */
  final def appendCredentials(queryMap: List[(String, String)]): List[(String, String)] =
    credentials.fold(queryMap)(c => "u" -> c.username :: "p" -> c.password :: queryMap)

  /** Update query params by appending db, user credentials
    *
    * @param db       - database name
    * @param queryMap - query parameter list
    * @return         - new updated query parameter list
    */
  final def appendCredentials(
      db: String,
      queryMap: List[(String, String)]
  ): List[(String, String)] =
    credentials.fold("db" -> db :: queryMap)(c =>
      List("db" -> db, "u" -> c.username, "p" -> c.password) ::: queryMap
    )

  final def appendCredentials(dbName: String, query: String): List[(String, String)] =
    credentials.fold(List("db" -> dbName, "q" -> query))(c =>
      List(
        "db" -> dbName,
        "u"  -> c.username,
        "p"  -> c.password,
        "q"  -> query
      )
    )

  /** Produce query parameters map for string parameter, with embedding credentials
    *
    * @param query - query string parameter
    * @return      - query parameters
    */
  final def appendCredentials(query: String): List[(String, String)] =
    appendCredentials("q" -> query :: Nil)
}
