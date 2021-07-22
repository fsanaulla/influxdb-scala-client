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

package com.github.fsanaulla.chronicler.ahc.shared.handlers

import com.github.fsanaulla.chronicler.ahc.shared.Uri
import com.github.fsanaulla.chronicler.core.components.RequestExecutor
import com.github.fsanaulla.chronicler.core.gzip
import io.netty.handler.codec.http.HttpHeaderValues.GZIP_DEFLATE
import org.asynchttpclient.{AsyncHttpClient, Response}

import scala.compat.java8.FutureConverters._
import scala.concurrent.Future

private[ahc] final class AhcRequestExecutor()(implicit client: AsyncHttpClient)
    extends RequestExecutor[Future, Response, Uri, String] {

  /** Execute uri
    *
    * @param uri - request uri
    * @return    - Return wrapper response
    */
  override def get(uri: Uri, compress: Boolean): Future[Response] = {
    val req             = client.prepareGet(uri.mkUrl)
    val maybeCompressed = if (compress) req.setHeader("Accept-Encoding", GZIP_DEFLATE) else req

    maybeCompressed.execute.toCompletableFuture.toScala
  }

  override def post(
      uri: Uri,
      body: String,
      compress: Boolean
  ): Future[Response] = {
    val req = client.preparePost(uri.mkUrl)
    val maybeEncoded = if (compress) {
      val (length, data) = gzip.compress(body.getBytes())
      // it fails with input stream, using byte array instead
      req
        .setBody(data)
        .setHeader("Content-Encoding", "gzip")
        .setHeader("Content-Type", "application/octet-stream")
        .setHeader("Content-Length", length)
    } else req.setBody(body.getBytes())

    maybeEncoded
      .execute()
      .toCompletableFuture
      .toScala
  }

  /** Quite simple post operation for creating
    *
    * @param uri - request uri
    */
  override def post(uri: Uri): Future[Response] = {
    client
      .preparePost(uri.mkUrl)
      .execute()
      .toCompletableFuture
      .toScala
  }
}
