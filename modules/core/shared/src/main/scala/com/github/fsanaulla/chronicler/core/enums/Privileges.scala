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

package com.github.fsanaulla.chronicler.core.enums

import enumeratum.EnumEntry

import scala.collection.immutable

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 29.07.17
  */
sealed trait Privilege extends EnumEntry

object Privileges extends enumeratum.Enum[Privilege] {
  val values: immutable.IndexedSeq[Privilege] = findValues

  case object READ  extends Privilege
  case object WRITE extends Privilege
  case object ALL   extends Privilege
  case object NO_PRIVILEGES extends Privilege {
    override def toString: String = "NO PRIVILEGES"
  }
}
