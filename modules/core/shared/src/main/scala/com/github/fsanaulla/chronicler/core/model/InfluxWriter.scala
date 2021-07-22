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

package com.github.fsanaulla.chronicler.core.model

import java.io.{Serializable => JSerializable}

import com.github.fsanaulla.chronicler.core.alias.ErrorOr

import scala.annotation.implicitNotFound

/** Return string must be in following format
  * <measurement>,[<tag-key>=<tag-value>...] [<field-key>=<field-value>,<field2-key>=<field2-value>...] [unix-nano-timestamp]
  * Look on official documentation [https://docs.influxdata.com/influxdb/v1.2/write_protocols/line_protocol_reference/]
  */
@implicitNotFound(
  "No InfluxWriter found for type ${T}. Try to implement an implicit InfluxWriter for this type."
)
trait InfluxWriter[T] extends JSerializable {
  def write(obj: T): ErrorOr[String]
}

object InfluxWriter {
  def apply[T](implicit writer: InfluxWriter[T]): InfluxWriter[T] = writer
}
