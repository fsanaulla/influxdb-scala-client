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
sealed abstract class Consistency extends EnumEntry with HasNone {
  override def toString: String = entryName
}

object Consistencies extends enumeratum.Enum[Consistency] {
  val values: immutable.IndexedSeq[Consistency] = findValues

  case object One    extends Consistency { override val entryName: String = "one"    }
  case object Quorum extends Consistency { override val entryName: String = "quorum" }
  case object All    extends Consistency { override val entryName: String = "all"    }
  case object Any    extends Consistency { override val entryName: String = "any"    }
  case object None   extends Consistency { override val isNone: Boolean = true       }
}
