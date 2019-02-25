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

package com.github.fsanaulla.chronicler.ahc.management

import com.github.fsanaulla.chronicler.ahc.shared.InfluxAhcClient
import com.github.fsanaulla.chronicler.ahc.shared.alias._
import com.github.fsanaulla.chronicler.ahc.shared.handlers.{AhcJsonHandler, AhcQueryBuilder, AhcRequestExecutor, AhcResponseHandler}
import com.github.fsanaulla.chronicler.core.ManagementClient
import com.github.fsanaulla.chronicler.core.model.{InfluxCredentials, PingResult}
import com.github.fsanaulla.chronicler.core.typeclasses.FlatMap
import com.softwaremill.sttp.{Response, Uri}
import jawn.ast.JValue
import org.asynchttpclient.AsyncHttpClientConfig

import scala.concurrent.{ExecutionContext, Future}

final class AhcManagementClient(host: String,
                                port: Int,
                                credentials: Option[InfluxCredentials],
                                asyncClientConfig: Option[AsyncHttpClientConfig])
                               (implicit ex: ExecutionContext)
  extends InfluxAhcClient(asyncClientConfig) with ManagementClient[Future, Request, Response[JValue], Uri, String] {

  implicit val qb: AhcQueryBuilder = new AhcQueryBuilder(host, port, credentials)
  implicit val re: AhcRequestExecutor = new AhcRequestExecutor
  implicit val rh: AhcResponseHandler = new AhcResponseHandler(new AhcJsonHandler)
  implicit val fm: FlatMap[Future] = new FlatMap[Future] {
    def flatMap[A, B](fa: Future[A])(f: A => Future[B]): Future[B] = fa.flatMap(f)
  }

  override def ping(isVerbose: Boolean = false): Future[PingResult] = {
    val queryParams = if (isVerbose) Map("verbose" -> "true") else Map.empty[String, String]
    re
      .execute(re.buildRequest(qb.buildQuery("/ping", queryParams)))
      .flatMap(rh.toPingResult)
  }
}
