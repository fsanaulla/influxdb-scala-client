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
import com.github.fsanaulla.chronicler.core.enums.Destinations
import com.github.fsanaulla.chronicler.core.model.InfluxCredentials
import com.github.fsanaulla.chronicler.core.query.SubscriptionsManagementQuery
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 21.08.17
  */
class SubscriptionsManagementQuerySpec
    extends AnyFlatSpec
    with Matchers
    with SubscriptionsManagementQuery[Uri] {

  trait AuthEnv {
    val credentials: Option[InfluxCredentials] = Some(InfluxCredentials("admin", "admin"))
    implicit val qb: AkkaQueryBuilder          = new AkkaQueryBuilder("http", "localhost", 8086, credentials)
  }

  trait NonAuthEnv {
    implicit val qb: AkkaQueryBuilder = new AkkaQueryBuilder("http", "localhost", 8086, None)
  }

  val subName                         = "subs"
  val dbName                          = "db"
  val rpName                          = "rp"
  val destType: Destinations.ANY.type = Destinations.ANY
  val hosts: Seq[String]              = Seq("host1", "host2")
  val resHosts: String                = Seq("host1", "host2").map(str => s"'$str'").mkString(", ")

  val createRes =
    s"CREATE SUBSCRIPTION $subName ON $dbName.$rpName DESTINATIONS $destType $resHosts"

  "SubscriptionsManagementQuery" should "create subs query" in new AuthEnv {
    createSubscriptionQuery(subName, dbName, rpName, destType, hosts) shouldEqual
      queryTesterAuth(createRes)(credentials.get)
  }

  it should "create subs query without auth" in new NonAuthEnv {
    createSubscriptionQuery(subName, dbName, rpName, destType, hosts) shouldEqual queryTester(
      createRes
    )
  }

  val dropRes = s"DROP SUBSCRIPTION $subName ON $dbName.$rpName"

  it should "drop subs query" in new AuthEnv {
    dropSubscriptionQuery(subName, dbName, rpName) shouldEqual
      queryTesterAuth(dropRes)(credentials.get)
  }

  it should "drop subs query without auth" in new NonAuthEnv {
    dropSubscriptionQuery(subName, dbName, rpName) shouldEqual queryTester(dropRes)
  }

  val showRes = "SHOW SUBSCRIPTIONS"

  it should "show subs query" in new AuthEnv {
    showSubscriptionsQuery shouldEqual
      queryTesterAuth(showRes)(credentials.get)
  }

  it should "show subs query without auth" in new NonAuthEnv {
    showSubscriptionsQuery shouldEqual queryTester(showRes)
  }
}
