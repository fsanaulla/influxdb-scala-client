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

import com.github.fsanaulla.chronicler.core.alias.{ErrorOr, ResponseCode, Tags, Values}
import com.github.fsanaulla.chronicler.core.either
import com.github.fsanaulla.chronicler.core.either._
import com.github.fsanaulla.chronicler.core.model._
import org.typelevel.jawn.ast.JArray

import scala.reflect.ClassTag

/** Response handling functionality, it's provide method's that generalize
  * response handle flow, for every backend implementation
  *
  * @tparam R - Backend HTTP response type, for example for Akka HTTP backend - HttpResponse
  */
class ResponseHandler[G[_], R](
    jsonHandler: JsonHandler[G, R]
)(implicit F: Functor[G], A: Apply[G]) {

  /** Handling ping response
    *
    * @since 0.5.1
    */
  final def pingResult(response: R): G[ErrorOr[InfluxDBInfo]] = {
    if (isPingCode(jsonHandler.responseCode(response))) A.pure(jsonHandler.databaseInfo(response))
    else F.map(errorHandler(response))(Left(_))
  }

  /** Method for handling HTTP responses with empty body
    *
    * @param response - backend response value
    * @return         - Result in future container
    */
  final def writeResult(response: R): G[ErrorOr[ResponseCode]] = {
    jsonHandler.responseCode(response) match {
      case code if isSuccessful(code) && code != 204 =>
        F.map(jsonHandler.responseErrorMsgOpt(response)) { ethErr =>
          ethErr.flatMapRight { err =>
            err.fold[ErrorOr[ResponseCode]](Right(code))(str => Left(InfluxException(code, str)))
          }
        }
      case 204 =>
        A.pure(Right(204))
      case _ =>
        F.map(errorHandler(response))(Left(_))
    }
  }

  /** Handling HTTP responses with on fly body deserialization into JArray value
    *
    * @param response - backend response value
    * @return         - Query result of JArray in future container
    */
  final def queryResultJson(response: R): G[ErrorOr[Array[JArray]]] =
    jsonHandler.responseCode(response).intValue() match {
      case code if isSuccessful(code) =>
        F.map(jsonHandler.responseBody(response)) { body =>
          body.mapRight { json =>
            jsonHandler.queryResult(json) match {
              case Some(arr) => arr
              case _         => Array.empty[JArray]
            }
          }
        }
      case _ =>
        F.map(errorHandler(response))(Left(_))
    }

  /** Handling HTTP response with GROUP BY clause in the query
    *
    * @param response - backend response
    * @return         - grouped result
    */
  final def groupedResultJson(response: R): G[ErrorOr[Array[(Tags, Values)]]] =
    jsonHandler.responseCode(response) match {
      case code if isSuccessful(code) =>
        F.map(jsonHandler.responseBody(response)) { ethRes =>
          ethRes.mapRight(jv =>
            jsonHandler.groupedResult(jv) match {
              case Some(arr) => arr
              case _         => Array.empty
            }
          )
        }
      case _ =>
        F.map(errorHandler(response))(Left(_))
    }

  /** Method for handling HTtp responses with non empty body, that contains multiple response.
    *
    * deserialize to Seq[JArray]
    *
    * @param response - backend response value
    * @return         - Query result with multiple response values
    */
  final def bulkQueryResultJson(response: R): G[ErrorOr[Array[Array[JArray]]]] =
    jsonHandler.responseCode(response) match {
      case code if isSuccessful(code) =>
        F.map(jsonHandler.responseBody(response)) { ethRes =>
          ethRes.mapRight(resp =>
            jsonHandler.bulkResult(resp) match {
              case Some(arr) => arr
              case _         => Array.empty
            }
          )
        }
      case _ =>
        F.map(errorHandler(response))(Left(_))
    }

  /** Method for handling Info based HTTP responses, with possibility for future deserialization.
    *
    * @param response - backend response value
    * @param f        - function that transform into value of type [B]
    * @tparam A       - entity for creating full Info object
    * @tparam B       - info object
    * @return         - Query result of [B] in future container
    */
  final def toComplexQueryResult[A: ClassTag: InfluxReader, B: ClassTag](
      response: R,
      f: (String, Array[A]) => B
  ): G[ErrorOr[Array[B]]] = {
    jsonHandler.responseCode(response) match {
      case code if isSuccessful(code) =>
        F.map(jsonHandler.responseBody(response)) { body =>
          body
            .flatMapRight(jsonHandler.groupedSystemInfo[A])
            .mapRight { arr =>
              arr.map { case (dbName, queries) => f(dbName, queries) }
            }
        }
      case _ =>
        F.map(errorHandler(response))(Left(_))
    }
  }

  /** Extract HTTP response body, and transform it to A
    *
    * @param response backend response
    * @tparam A - Deserializer entity type
    * @return - Query result in future container
    */
  final def queryResult[A: ClassTag](
      response: R
  )(implicit rd: InfluxReader[A]): G[ErrorOr[Array[A]]] =
    F.map(queryResultJson(response)) { jvRes =>
      jvRes
        .mapRight(_.map(rd.read))
        .mapRight(either.array[Throwable, A])
        .joinRight
    }

  /** *
    * Handler error codes by it's value
    *
    * @param response - response for extracting error message
    * @return         - InfluxException wrraped in container type
    */
  final def errorHandler(response: R): G[Throwable] =
    F.map(jsonHandler.responseErrorMsg(response)) { ethErr =>
      ethErr
        .mapRight(InfluxException(jsonHandler.responseCode(response), _))
        // merging parsing level issues with request level issues
        .merge
    }

  /** *
    * Get CQ information from Response
    *
    * @param response - Response object
    * @param reader - implicit influx reader, predefined
    * @return - CQ results
    */
  def toCqQueryResult(
      response: R
  )(implicit reader: InfluxReader[ContinuousQuery]): G[ErrorOr[Array[ContinuousQueryInfo]]] = {
    toComplexQueryResult[ContinuousQuery, ContinuousQueryInfo](
      response,
      (dbName: String, queries: Array[ContinuousQuery]) => ContinuousQueryInfo(dbName, queries)
    )
  }

  /** *
    * Get Shard info information from Response
    *
    * @param response - Response object
    * @param reader - implicit influx reader, predefined
    * @return - Shard info  results
    */
  final def toShardQueryResult(
      response: R
  )(implicit reader: InfluxReader[Shard]): G[ErrorOr[Array[ShardInfo]]] = {
    toComplexQueryResult[Shard, ShardInfo](
      response,
      (dbName: String, shards: Array[Shard]) => ShardInfo(dbName, shards)
    )
  }

  /** *
    * Get Subscription info information from Response
    *
    * @param response - Response object
    * @param reader - implicit influx reader, predefined
    * @return - Subscription info  results
    */
  final def toSubscriptionQueryResult(
      response: R
  )(implicit reader: InfluxReader[Subscription]): G[ErrorOr[Array[SubscriptionInfo]]] = {
    toComplexQueryResult[Subscription, SubscriptionInfo](
      response,
      (dbName: String, subscriptions: Array[Subscription]) =>
        SubscriptionInfo(dbName, subscriptions)
    )
  }

  /** *
    * Get Shard group info information from Response
    *
    * @param response - Response object
    * @param reader - implicit influx reader, predefined
    * @return - Shard group info  results
    */
  final def toShardGroupQueryResult(
      response: R
  )(implicit reader: InfluxReader[ShardGroup]): G[ErrorOr[Array[ShardGroupsInfo]]] = {
    toComplexQueryResult[ShardGroup, ShardGroupsInfo](
      response,
      (dbName: String, shardGroups: Array[ShardGroup]) => ShardGroupsInfo(dbName, shardGroups)
    )
  }

  /** *
    * Check response for success
    *
    * @param code - response code
    * @return     - is it success
    */
  final def isSuccessful(code: Int): Boolean = code >= 200 && code < 300

  /** *
    * Check for ping response status code
    *
    * @param code - response code
    */
  final def isPingCode(code: Int): Boolean = code == 200 || code == 204
}
