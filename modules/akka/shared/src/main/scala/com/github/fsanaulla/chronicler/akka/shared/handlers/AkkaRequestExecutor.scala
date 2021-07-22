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

package com.github.fsanaulla.chronicler.akka.shared.handlers

import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{`Accept-Encoding`, `Content-Encoding`, HttpEncodings}
import akka.http.scaladsl.{HttpExt, HttpsConnectionContext}
import com.github.fsanaulla.chronicler.core.components.RequestExecutor

import scala.concurrent.Future

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 15.03.18
  */
private[akka] final class AkkaRequestExecutor(ctx: HttpsConnectionContext)(implicit http: HttpExt)
    extends RequestExecutor[Future, HttpResponse, Uri, RequestEntity] {

  /** Execute uri
    *
    * @param uri - request uri
    * @return    - Return wrapper response
    */
  override def get(uri: Uri, compressed: Boolean): Future[HttpResponse] = {
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = uri,
      // default headers
      headers = if (compressed) `Accept-Encoding`(HttpEncodings.gzip) :: Nil else Nil
    )

    http.singleRequest(
      request,
      connectionContext = ctx
    )
  }

  override def post(
      uri: Uri,
      body: RequestEntity,
      compressed: Boolean
  ): Future[HttpResponse] = {
    val headers = if (compressed) `Content-Encoding`(HttpEncodings.gzip) :: Nil else Nil
    val entity  = if (compressed) body.transformDataBytes(Gzip.encoderFlow) else body

    val request = HttpRequest(HttpMethods.POST, uri)
      .withHeaders(headers)
      .withEntity(entity)

    http.singleRequest(request, connectionContext = ctx)
  }

  /** Quite simple post operation for creating
    *
    * @param uri - request uri
    */
  override def post(uri: Uri): Future[HttpResponse] = {
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = uri
    )

    http.singleRequest(
      request,
      connectionContext = ctx
    )
  }
}
