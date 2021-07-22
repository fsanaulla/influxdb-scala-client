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

package com.github.fsanaulla.chronicler.akka.query

import akka.http.scaladsl.model.Uri
import com.github.fsanaulla.chronicler.akka.shared.handlers.AkkaQueryBuilder
import com.github.fsanaulla.chronicler.core.model.InfluxCredentials
import com.github.fsanaulla.chronicler.core.query.ContinuousQueries
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 10.08.17
  */
class ContinuousQueriesSpec extends AnyFlatSpec with Matchers with ContinuousQueries[Uri] {

  trait AuthEnv {
    val credentials: Option[InfluxCredentials] = Some(InfluxCredentials("admin", "admin"))
    implicit val qb: AkkaQueryBuilder          = new AkkaQueryBuilder("http", "localhost", 8086, credentials)
  }

  trait NonAuthEnv {
    implicit val qb: AkkaQueryBuilder = new AkkaQueryBuilder("http", "localhost", 8086, None)
  }

  val db    = "mydb"
  val cq    = "bee_cq"
  val query = "SELECT mean(bees) AS mean_bees INTO aggregate_bees FROM farm GROUP BY time(30m)"

  "ContinuousQuerys operation" should "generate correct show query" in new AuthEnv {
    showCQQuery shouldEqual queryTesterAuth("SHOW CONTINUOUS QUERIES")(credentials.get)
  }

  it should "generate correct drop query" in new AuthEnv {
    dropCQQuery(db, cq) shouldEqual queryTesterAuth(s"DROP CONTINUOUS QUERY $cq ON $db")(
      credentials.get
    )
  }

  it should "generate correct create query" in new AuthEnv {
    createCQQuery(db, cq, query) shouldEqual queryTesterAuth(
      s"CREATE CONTINUOUS QUERY $cq ON $db BEGIN $query END"
    )(credentials.get)
  }

  it should "generate correct show query without auth" in new NonAuthEnv {
    showCQQuery shouldEqual queryTester("SHOW CONTINUOUS QUERIES")
  }

  it should "generate correct drop query without auth" in new NonAuthEnv {
    dropCQQuery(db, cq) shouldEqual queryTester(s"DROP CONTINUOUS QUERY $cq ON $db")
  }

  it should "generate correct create query without auth" in new NonAuthEnv {
    createCQQuery(db, cq, query) shouldEqual queryTester(
      s"CREATE CONTINUOUS QUERY $cq ON $db BEGIN $query END"
    )
  }
}
