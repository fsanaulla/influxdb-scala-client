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
import com.github.fsanaulla.chronicler.core.query.ShardManagementQuery
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 19.08.17
  */
class ShardManagementQuerySpec extends AnyFlatSpec with Matchers with ShardManagementQuery[Uri] {

  trait AuthEnv {
    val credentials: Option[InfluxCredentials] = Some(InfluxCredentials("admin", "admin"))
    implicit val qb: AkkaQueryBuilder          = new AkkaQueryBuilder("http", "localhost", 8086, credentials)
  }

  trait NonAuthEnv {
    implicit val qb: AkkaQueryBuilder = new AkkaQueryBuilder("http", "localhost", 8086, None)
  }

  "ShardManagementQuery" should "drop shard by id" in new AuthEnv {
    dropShardQuery(5) shouldEqual queryTesterAuth("DROP SHARD 5")(credentials.get)
  }

  it should "drop shard by id without auth" in new NonAuthEnv {
    dropShardQuery(5) shouldEqual queryTester("DROP SHARD 5")
  }

  it should "show shards" in new AuthEnv {
    showShardsQuery shouldEqual queryTesterAuth("SHOW SHARDS")(credentials.get)
  }

  it should "show shards without auth" in new NonAuthEnv {
    showShardsQuery shouldEqual queryTester("SHOW SHARDS")
  }

  it should "show shard groups" in new AuthEnv {
    showShardGroupsQuery shouldEqual queryTesterAuth("SHOW SHARD GROUPS")(
      credentials.get
    )
  }

  it should "show shard groups without auth" in new NonAuthEnv {
    showShardGroupsQuery shouldEqual queryTester("SHOW SHARD GROUPS")
  }
}
