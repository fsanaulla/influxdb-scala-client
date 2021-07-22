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

package com.github.fsanaulla.chronicler.akka.management

import _root_.akka.actor.ActorSystem
import _root_.akka.http.scaladsl.HttpsConnectionContext
import akka.http.scaladsl.model.{HttpResponse, RequestEntity, Uri}
import akka.stream.ActorMaterializer
import com.github.fsanaulla.chronicler.akka.shared.InfluxAkkaClient
import com.github.fsanaulla.chronicler.akka.shared.handlers._
import com.github.fsanaulla.chronicler.core.ManagementClient
import com.github.fsanaulla.chronicler.core.alias.ErrorOr
import com.github.fsanaulla.chronicler.core.model._

import scala.concurrent.{ExecutionContext, Future}

final class AkkaManagementClient(
    host: String,
    port: Int,
    credentials: Option[InfluxCredentials],
    httpsContext: Option[HttpsConnectionContext],
    terminateActorSystem: Boolean
)(implicit
    val ex: ExecutionContext,
    val system: ActorSystem,
    val F: Functor[Future],
    val FK: FunctionK[Future, Future]
) extends InfluxAkkaClient(terminateActorSystem, httpsContext)
    with ManagementClient[Future, Future, HttpResponse, Uri, RequestEntity] {

  implicit val mat: ActorMaterializer  = ActorMaterializer()
  implicit val qb: AkkaQueryBuilder    = new AkkaQueryBuilder(schema, host, port, credentials)
  implicit val jh: AkkaJsonHandler     = new AkkaJsonHandler(new AkkaBodyUnmarshaller(false))
  implicit val re: AkkaRequestExecutor = new AkkaRequestExecutor(ctx)
  implicit val rh: AkkaResponseHandler = new AkkaResponseHandler(jh)

  override def ping: Future[ErrorOr[InfluxDBInfo]] = {
    re.get(qb.buildQuery("/ping"), compressed = false)
      .flatMap(rh.pingResult)
  }
}
