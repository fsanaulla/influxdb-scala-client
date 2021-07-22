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

import com.github.fsanaulla.chronicler.core.alias.{ErrorOr, Tags, Values}
import com.github.fsanaulla.chronicler.core.either
import com.github.fsanaulla.chronicler.core.either._
import com.github.fsanaulla.chronicler.core.headers.{buildHeader, versionHeader}
import com.github.fsanaulla.chronicler.core.jawn._
import com.github.fsanaulla.chronicler.core.model.{
  Functor,
  InfluxDBInfo,
  InfluxReader,
  ParsingException
}
import org.typelevel.jawn.ast.{JArray, JValue}

import scala.reflect.ClassTag

/** *
  * JSON handler for extracting body, code, headers
  *
  * @tparam F - parsing effect
  * @tparam R - Response type
  */
abstract class JsonHandler[F[_], R](implicit F: Functor[F]) {

  /** *
    * Extract response http code
    */
  def responseCode(response: R): Int

  /** *
    * Extract response headers
    */
  def responseHeader(response: R): Seq[(String, String)]

  /** *
    * Extracting JSON from Response
    *
    * @param response   - HTTP response
    */
  def responseBody(response: R): F[ErrorOr[JValue]]

  /** *
    * Used to extract database info from ping response
    */
  final def databaseInfo(response: R): ErrorOr[InfluxDBInfo] = {
    val headers = responseHeader(response)
    val result = for {
      build   <- headers.collectFirst { case (k, v) if k.equalsIgnoreCase(buildHeader) => v }
      version <- headers.collectFirst { case (k, v) if k.equalsIgnoreCase(versionHeader) => v }
    } yield InfluxDBInfo(build, version)

    result.toRight(new ParsingException(s"Can't find $buildHeader or $versionHeader"))
  }

  /** Extract error message from response
    *
    * @param response - Response
    * @return - Error Message
    */
  final def responseErrorMsg(response: R): F[ErrorOr[String]] =
    F.map(responseBody(response))(_.mapRight(_.get("error").asString))

  /** Extract optional error message from response
    *
    * @param response - Response JSON body
    * @return - optional error message
    */
  final def responseErrorMsgOpt(response: R): F[ErrorOr[Option[String]]] =
    F.map(responseBody(response)) { bd =>
      bd.mapRight(_.firstResult)
        .mapRight(_.flatMap(_.get("error").getString))
    }

  /** Extract influx points from JSON, represented as Arrays
    *
    * @param js - JSON value
    * @return - optional array of points
    */
  final def queryResult(js: JValue): Option[Array[JArray]] =
    js.firstResult.flatMap { json =>
      json.firstSeries
        .flatMap(_.valuesArray)
        .map(_.flatMap(_.array))
    }

  /** *
    * Extract influx point grouped by some criteria
    *
    * @param js - JSON payload
    * @return   - array of pairs (grouping key, grouped value)
    */
  final def groupedResult(js: JValue): Option[Array[(Tags, Values)]] =
    js.firstResult
      .flatMap(_.seriesArray)
      .map(_.flatMap(_.obj))
      .map { arr =>
        arr.flatMap { obj =>
          val tags   = obj.tags.obj.map(_.vs.values.map(_.asString).toArray.sorted)
          val values = obj.valuesArray.map(_.flatMap(_.array))

          for {
            tg <- tags
            vl <- values
          } yield tg -> vl
        }
      }

  /** Extract bulk result from JSON
    *
    * @param js - JSON value
    * @return - Array of points
    */
  final def bulkResult(js: JValue): Option[Array[Array[JArray]]] = {
    js.resultsArray
      .map(_.flatMap(_.firstSeries))
      .map(_.flatMap(_.valuesArray))
      .map(_.map(_.flatMap(_.array)))
  }

  /** Extract Measurement name -> Measurement points array
    *
    * @param js - JSON value
    * @return - array of meas name -> meas points
    */
  final def groupedSystemInfoJs(js: JValue): Option[Array[(String, Array[JArray])]] = {
    js.firstResult
      .flatMap(_.seriesArray)
      .map(_.flatMap(_.obj))
      .map { arr =>
        arr.flatMap { obj =>
          val measurement = obj.get("name").asString
          val cqInfo      = obj.valuesArray.map(_.flatMap(_.array))

          cqInfo.map(measurement -> _)
        }
      }
  }

  /** Extract Measurement name and values from it from JSON
    *
    * @param js - JSON value
    * @return - Array of pairs
    */
  final def groupedSystemInfo[T: ClassTag](
      js: JValue
  )(implicit rd: InfluxReader[T]): ErrorOr[Array[(String, Array[T])]] = {
    groupedSystemInfoJs(js) match {
      case Some(arr) =>
        either.array(arr.map { case (k, v) =>
          either.array[Throwable, T](v.map(rd.read)).mapRight(k -> _)
        })
      case _ =>
        Right(Array.empty)
    }
  }
}
