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

package com.github.fsanaulla.chronicler.macros

import com.github.fsanaulla.chronicler.core.model.InfluxReader
import com.github.fsanaulla.chronicler.macros.annotations.{field, tag, timestamp}
import com.github.fsanaulla.chronicler.macros.auto._
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.typelevel.jawn.ast._

class MacroReaderSpec extends AnyWordSpec with Matchers with EitherValues {
  "InfluxReader" should {
    "read" should {
      case class Test(@tag name: String, @tag surname: Option[String], @field age: Int)
      val rd: InfluxReader[Test] = InfluxReader[Test]

      "with None ignoring time" in {
        rd.read(
          JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
        ).value shouldEqual Test("Fz", None, 4)
      }

      "with Some and ignore time" in {
        rd.read(
          JArray(Array(JString("2015-08-04T19:05:14Z"), JNum(4), JString("Fz"), JString("Sr")))
        ).value shouldEqual Test("Fz", Some("Sr"), 4)
      }

      case class Test1(
          @tag name: String,
          @tag surname: Option[String],
          @field age: Int,
          @timestamp time: Long
      )
      val rd1: InfluxReader[Test1] = InfluxReader[Test1]

      "with timestamp" in {
        rd1
          .read(
            JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
          )
          .value shouldEqual Test1("Fz", None, 4, 1438715114318570484L)

        rd1
          .read(JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull)))
          .value shouldEqual Test1("Fz", None, 4, 1438715114318570484L)
      }
    }

    "readUnsafe" should {
      case class Test(@tag name: String, @tag surname: Option[String], @field age: Int)
      val rd: InfluxReader[Test] = InfluxReader[Test]

      "with None ignoring time" in {
        rd.readUnsafe(
          JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
        ).shouldEqual(Test("Fz", None, 4))
      }

      "with Some and ignore time" in {
        rd.readUnsafe(
          JArray(Array(JString("2015-08-04T19:05:14Z"), JNum(4), JString("Fz"), JString("Sr")))
        ).shouldEqual(Test("Fz", Some("Sr"), 4))
      }

      case class Test1(
          @tag name: String,
          @tag surname: Option[String],
          @field age: Int,
          @timestamp time: Long
      )
      val rd1: InfluxReader[Test1] = InfluxReader[Test1]

      "with timestamp" in {
        rd1
          .readUnsafe(
            JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
          )
          .shouldEqual(Test1("Fz", None, 4, 1438715114318570484L))

        rd1
          .readUnsafe(JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull)))
          .shouldEqual(Test1("Fz", None, 4, 1438715114318570484L))
      }
    }
  }
}
