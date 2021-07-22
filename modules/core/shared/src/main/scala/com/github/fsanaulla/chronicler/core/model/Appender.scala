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

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 27.08.17
  */
trait Appender {
  final def append(measurement: String, serializedEntity: String): String =
    measurement + "," + serializedEntity

  final def append(measurement: String, serializedEntitys: Seq[String]): String =
    serializedEntitys.map(s => measurement + "," + s).mkString("\n")

}
