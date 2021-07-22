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

package com.github.fsanaulla.chronicler.akka.query.unit

import akka.http.scaladsl.model.Uri
import com.github.fsanaulla.chronicler.akka.shared.handlers.AkkaQueryBuilder
import com.github.fsanaulla.chronicler.core.enums.{Consistencies, Epochs, Precisions}
import com.github.fsanaulla.chronicler.core.model.InfluxCredentials
import com.github.fsanaulla.chronicler.core.query.DatabaseOperationQuery
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 27.07.17
  */
class DatabaseOperationQuerySpec
    extends AnyFlatSpec
    with Matchers
    with DatabaseOperationQuery[Uri] {

  trait AuthEnv {
    val credentials: Option[InfluxCredentials] = Some(InfluxCredentials("admin", "admin"))
    implicit val qb: AkkaQueryBuilder          = new AkkaQueryBuilder("http", "localhost", 8086, credentials)
  }

  trait NonAuthEnv {
    implicit val qb: AkkaQueryBuilder = new AkkaQueryBuilder("http", "localhost", 8086, None)
  }

  val testDB    = "db"
  val testQuery = "SELECT * FROM test"

  "DatabaseOperationQuery" should "return correct write query" in new AuthEnv {

    write(testDB, Consistencies.One, Precisions.Nanoseconds, None)
      .toString() shouldEqual queryTester(
      "/write",
      List(
        "db"          -> testDB,
        "u"           -> credentials.get.username,
        "p"           -> credentials.get.password,
        "consistency" -> "one",
        "precision"   -> "ns"
      )
    )

    write(testDB, Consistencies.All, Precisions.Nanoseconds, None)
      .toString() shouldEqual queryTester(
      "/write",
      List(
        "db"          -> testDB,
        "u"           -> credentials.get.username,
        "p"           -> credentials.get.password,
        "consistency" -> "all",
        "precision"   -> "ns"
      )
    )
  }

  it should "return correct write query without auth " in new NonAuthEnv {
    write(testDB, Consistencies.One, Precisions.Nanoseconds, None).toString() shouldEqual
      queryTester(
        "/write",
        List("db" -> testDB, "consistency" -> "one", "precision" -> "ns")
      )

    write(testDB, Consistencies.One, Precisions.Microseconds, None).toString() shouldEqual
      queryTester(
        "/write",
        List("db" -> testDB, "consistency" -> "one", "precision" -> "u")
      )
  }

  it should "return correct single read query" in new AuthEnv {

    val queryPrms: List[(String, String)] = List(
      "db"    -> testDB,
      "u"     -> credentials.get.username,
      "p"     -> credentials.get.password,
      "epoch" -> "ns",
      "q"     -> "SELECT * FROM test"
    )
    singleQuery(testDB, testQuery, Epochs.Nanoseconds, pretty = false).toString() shouldEqual
      queryTester("/query", queryPrms)
  }

  it should "return correct bulk read query" in new AuthEnv {

    val queryPrms: List[(String, String)] = List(
      "db"    -> testDB,
      "u"     -> credentials.get.username,
      "p"     -> credentials.get.password,
      "epoch" -> "ns",
      "q"     -> "SELECT * FROM test;SELECT * FROM test1"
    )
    bulkQuery(
      testDB,
      Seq("SELECT * FROM test", "SELECT * FROM test1"),
      Epochs.Nanoseconds,
      pretty = false
    ).toString() shouldEqual queryTester("/query", queryPrms)

    val queryPrms1: List[(String, String)] = List(
      "db"     -> testDB,
      "u"      -> credentials.get.username,
      "p"      -> credentials.get.password,
      "pretty" -> "true",
      "epoch"  -> "ns",
      "q"      -> "SELECT * FROM test;SELECT * FROM test1"
    )
    bulkQuery(
      testDB,
      Seq("SELECT * FROM test", "SELECT * FROM test1"),
      Epochs.Nanoseconds,
      pretty = true
    ).toString() shouldEqual queryTester("/query", queryPrms1)
  }
}
