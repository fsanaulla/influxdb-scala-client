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

package com.github.fsanaulla.chronicler.core

import com.github.fsanaulla.chronicler.core.api.{DatabaseApi, MeasurementApi}
import com.github.fsanaulla.chronicler.core.management.SystemManagement

import scala.reflect.ClassTag

/** Define necessary methods for providing IO operations
  *
  * @tparam F - request execution effect
  * @tparam G - response parser effect
  * @tparam R - response type
  * @tparam U - request uri type
  * @tparam E - request entity type
  */
trait IOClient[F[_], G[_], R, U, E] extends SystemManagement[F] with AutoCloseable {

  type Database       = DatabaseApi[F, G, R, U, E]
  type Measurement[A] = MeasurementApi[F, G, R, U, E, A]

  /** Get database instant
    *
    * @param dbName - database name
    * @return       - Backend related implementation of DatabaseApi
    */
  def database(dbName: String): Database

  /** Get measurement instance with execution type A
    *
    * @param dbName          - on which database
    * @param measurementName - which measurement
    * @tparam A              - measurement entity type
    * @return                - Backend related implementation of MeasurementApi
    */
  def measurement[A: ClassTag](dbName: String, measurementName: String): Measurement[A]
}
