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

import com.github.fsanaulla.chronicler.core.enums.{Destination, Privilege}

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 19.08.17
  */
final case class UserInfo(username: String, isAdmin: Boolean)
final case class UserPrivilegesInfo(database: String, privilege: Privilege)
final case class RetentionPolicyInfo(
    name: String,
    duration: String,
    shardGroupDuration: String,
    replication: Int,
    default: Boolean
)
final case class ContinuousQuery(cqName: String, query: String)
final case class ShardGroup(
    id: Int,
    dbName: String,
    rpName: String,
    startTime: String,
    endTime: String,
    expiryTime: String
)
final case class Shard(
    id: Int,
    dbName: String,
    rpName: String,
    shardGroup: Int,
    startTime: String,
    endTime: String,
    expiryTime: String,
    owners: String
)
final case class Subscription(
    rpName: String,
    subsName: String,
    destType: Destination,
    addresses: Array[String]
)
final case class QueryInfo(queryId: Int, query: String, dbName: String, duration: String)
final case class ContinuousQueryInfo(dbName: String, queries: Array[ContinuousQuery])
final case class ShardGroupsInfo(shardGroupName: String, shardGroups: Array[ShardGroup])
final case class ShardInfo(dbName: String, shards: Array[Shard])
final case class SubscriptionInfo(dbName: String, subscriptions: Array[Subscription])
final case class FieldInfo(fieldName: String, fieldType: String)
final case class TagValue(tag: String, value: String)
final case class InfluxDBInfo(build: String, version: String)
