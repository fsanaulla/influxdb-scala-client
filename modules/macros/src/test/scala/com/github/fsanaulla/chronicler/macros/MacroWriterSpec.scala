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

import com.github.fsanaulla.chronicler.core.model.InfluxWriter
import com.github.fsanaulla.chronicler.macros.annotations.writer.escape
import com.github.fsanaulla.chronicler.macros.annotations.{field, tag, timestamp}
import com.github.fsanaulla.chronicler.macros.auto._
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MacroWriterSpec extends AnyWordSpec with Matchers with EitherValues {
  "InfluxWriter" should {
    "write optional" should {
      case class WithOptional(
          @tag name: String,
          @tag surname: Option[String],
          @field school: String,
          @field age: Int
      )
      val wr: InfluxWriter[WithOptional] = InfluxWriter[WithOptional]

      "with None" in {
        wr.write(WithOptional("nm", None, "Berkly", 65))
          .value shouldEqual "name=nm school=\"Berkly\",age=65i"
      }

      "with Some" in {
        wr.write(WithOptional("nm", Some("sn"), "Berkly", 65))
          .value shouldEqual "name=nm,surname=sn school=\"Berkly\",age=65i"
      }
    }

    "write with timestamp" in {
      case class WithTimestamp(
          @tag name: String,
          @tag surname: Option[String],
          @field age: Int,
          @field school: String,
          @timestamp time: Long
      )

      val wr1: InfluxWriter[WithTimestamp] = InfluxWriter[WithTimestamp]
      wr1
        .write(WithTimestamp("nm", Some("sn"), 65, "Berkly", 1438715114318570484L))
        .value shouldEqual "name=nm,surname=sn age=65i,school=\"Berkly\" 1438715114318570484"
    }

    "write with double" in {
      case class WithDouble(
          @tag name: String,
          @tag surname: Option[String],
          @field mark: Double,
          @field school: String,
          @timestamp time: Long
      )

      val wr1: InfluxWriter[WithDouble] = InfluxWriter[WithDouble]
      wr1
        .write(
          WithDouble("nm", Some("sn"), 65.4, "Berkly", 1438715114318570484L)
        ) shouldEqual Right(
        "name=nm,surname=sn mark=65.4,school=\"Berkly\" 1438715114318570484"
      )

      case class WithDoubleAsASecondField(
          @tag name: String,
          @tag surname: Option[String],
          @field age: Int,
          @field mark: Double,
          @timestamp time: Long
      )

      val wr2: InfluxWriter[WithDoubleAsASecondField] = InfluxWriter[WithDoubleAsASecondField]
      wr2
        .write(
          WithDoubleAsASecondField("nm", Some("sn"), 1, 65.4, 1438715114318570484L)
        ) shouldEqual Right(
        "name=nm,surname=sn age=1i,mark=65.4 1438715114318570484"
      )
    }

    "write and escape " should {
      case class Test(@escape @tag name: String, @field age: Int)
      val wr: InfluxWriter[Test] = InfluxWriter[Test]

      "from ','" in {
        val t = Test("My,Name", 5)
        wr.write(t).value shouldEqual "name=My\\,Name age=5i"
      }

      "from '='" in {
        val t = Test("My=Name", 5)
        wr.write(t).value shouldEqual "name=My\\=Name age=5i"
      }

      "from ' '" in {
        val t = Test("My Name", 5)
        wr.write(t) shouldEqual Right("name=My\\ Name age=5i")
      }

      "from all special characters" in {
        val t = Test("My ,=Name", 5)
        wr.write(t).value shouldEqual "name=My\\ \\,\\=Name age=5i"
      }
    }
  }
}
