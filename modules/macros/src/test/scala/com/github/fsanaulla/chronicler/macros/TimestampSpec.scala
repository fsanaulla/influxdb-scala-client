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
import com.github.fsanaulla.chronicler.macros.annotations._
import com.github.fsanaulla.chronicler.macros.annotations.reader.{epoch, utc}
import com.github.fsanaulla.chronicler.macros.auto._
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.typelevel.jawn.ast._

class TimestampSpec extends AnyWordSpec with Matchers with EitherValues {

  "@timestamp" should {

    "deserialize" should {
      case class GeneralEpochTimestamp(
          @tag name: String,
          @tag surname: Option[String],
          @field age: Int,
          @timestamp time: Long
      )

      val epochRd = InfluxReader[GeneralEpochTimestamp]

      "epoch time" in {
        epochRd
          .read(
            JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
          )
          .value shouldEqual GeneralEpochTimestamp("Fz", None, 4, 1438715114318570484L)

        epochRd
          .read(JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull)))
          .value shouldEqual GeneralEpochTimestamp("Fz", None, 4, 1438715114318570484L)

        epochRd
          .readUnsafe(
            JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
          )
          .shouldEqual(GeneralEpochTimestamp("Fz", None, 4, 1438715114318570484L))

        epochRd
          .readUnsafe(JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull)))
          .shouldEqual(GeneralEpochTimestamp("Fz", None, 4, 1438715114318570484L))
      }

      case class GeneralUtcTimestamp(
          @tag name: String,
          @tag surname: Option[String],
          @field age: Int,
          @timestamp time: String
      )

      val utcRd = InfluxReader[GeneralUtcTimestamp]
      "utc time" in {
        utcRd
          .read(JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull)))
          .value shouldEqual GeneralUtcTimestamp("Fz", None, 4, "2015-08-04T19:05:14.318570484Z")

        utcRd
          .read(
            JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
          )
          .value shouldEqual GeneralUtcTimestamp("Fz", None, 4, "2015-08-04T19:05:14.318570484Z")

        utcRd
          .readUnsafe(JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull)))
          .shouldEqual(GeneralUtcTimestamp("Fz", None, 4, "2015-08-04T19:05:14.318570484Z"))

        utcRd
          .readUnsafe(
            JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
          )
          .shouldEqual(GeneralUtcTimestamp("Fz", None, 4, "2015-08-04T19:05:14.318570484Z"))
      }
    }
  }

  "@epoch @timestamp" should {
    "deserialize epoch time" in {
      case class EpochTimestamp(
          @tag name: String,
          @tag surname: Option[String],
          @field age: Int,
          @epoch @timestamp time: Long
      )
      val rd = InfluxReader[EpochTimestamp]

      rd.read(JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull)))
        .value shouldEqual EpochTimestamp("Fz", None, 4, 1438715114318570484L)

      rd.readUnsafe(JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull)))
        .shouldEqual(EpochTimestamp("Fz", None, 4, 1438715114318570484L))
    }
  }

  "@utc @timestamp" should {
    "deserialize utc time" in {
      case class UTCTimestamp(
          @tag name: String,
          @tag surname: Option[String],
          @field age: Int,
          @utc @timestamp time: String
      )
      val rd = InfluxReader[UTCTimestamp]

      rd.read(
        JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
      ).value shouldEqual UTCTimestamp("Fz", None, 4, "2015-08-04T19:05:14.318570484Z")

      rd.readUnsafe(
        JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
      ).shouldEqual(UTCTimestamp("Fz", None, 4, "2015-08-04T19:05:14.318570484Z"))
    }
  }
}
