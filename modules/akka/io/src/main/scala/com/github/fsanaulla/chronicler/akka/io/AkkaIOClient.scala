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

package com.github.fsanaulla.chronicler.akka.io

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpsConnectionContext
import akka.http.scaladsl.model.{HttpResponse, RequestEntity, Uri}
import akka.stream.ActorMaterializer
import com.github.fsanaulla.chronicler.akka.shared.InfluxAkkaClient
import com.github.fsanaulla.chronicler.akka.shared.handlers._
import com.github.fsanaulla.chronicler.akka.shared.implicits._
import com.github.fsanaulla.chronicler.core.IOClient
import com.github.fsanaulla.chronicler.core.alias.ErrorOr
import com.github.fsanaulla.chronicler.core.model.{InfluxCredentials, InfluxDBInfo}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

final class AkkaIOClient(
    host: String,
    port: Int,
    credentials: Option[InfluxCredentials],
    compress: Boolean,
    httpsContext: Option[HttpsConnectionContext],
    terminateActorSystem: Boolean
)(implicit ex: ExecutionContext, system: ActorSystem)
    extends InfluxAkkaClient(terminateActorSystem, httpsContext)
    with IOClient[Future, Future, HttpResponse, Uri, RequestEntity] {

  implicit val mat: ActorMaterializer  = ActorMaterializer()
  implicit val bb: AkkaBodyBuilder     = new AkkaBodyBuilder()
  implicit val qb: AkkaQueryBuilder    = new AkkaQueryBuilder(schema, host, port, credentials)
  implicit val jh: AkkaJsonHandler     = new AkkaJsonHandler(new AkkaBodyUnmarshaller(compress))
  implicit val re: AkkaRequestExecutor = new AkkaRequestExecutor(ctx)
  implicit val rh: AkkaResponseHandler = new AkkaResponseHandler(jh)

  override def database(dbName: String): AkkaDatabaseApi =
    new AkkaDatabaseApi(dbName, compress)

  override def measurement[A: ClassTag](
      dbName: String,
      measurementName: String
  ): AkkaMeasurementApi[A] =
    new AkkaMeasurementApi[A](dbName, measurementName, compress)

  override def ping: Future[ErrorOr[InfluxDBInfo]] = {
    re.get(qb.buildQuery("/ping", Nil), compressed = false)
      .flatMap(rh.pingResult)
  }
}
