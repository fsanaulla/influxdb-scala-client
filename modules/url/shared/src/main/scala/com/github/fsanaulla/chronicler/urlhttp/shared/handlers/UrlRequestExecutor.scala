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

package com.github.fsanaulla.chronicler.urlhttp.shared.handlers

import com.github.fsanaulla.chronicler.core.alias.{ErrorOr, Id}
import com.github.fsanaulla.chronicler.core.components.{JsonHandler, RequestExecutor}
import com.github.fsanaulla.chronicler.core.either._
import com.github.fsanaulla.chronicler.core.gzip
import com.github.fsanaulla.chronicler.core.jawn.RichJParser
import com.github.fsanaulla.chronicler.core.model.ParsingException
import com.github.fsanaulla.chronicler.urlhttp.shared.{ChroniclerSession, Url}
import org.typelevel.jawn.ast.{JArray, JParser}
import requests._

import scala.io.Source
import scala.util.Try

private[urlhttp] final class UrlRequestExecutor(jsonHandler: JsonHandler[Id, Response])
    extends RequestExecutor[Try, Response, Url, String] {

  /** Execute uri
    *
    * @param url - request uri
    * @return    - Return wrapper response
    */
  override def get(url: Url, compress: Boolean): Try[Response] = {
    val headers =
      if (compress) "Accept-Encoding" -> "gzip" :: Nil else Nil

    url.make.flatMap { address =>
      Try {
        requests.get
          .copy(sess = new ChroniclerSession)
          .apply(
            address,
            params = url.params,
            headers = headers,
            autoDecompress = false
          )
      }
    }
  }

  override def post(url: Url): Try[Response] = {
    url.make.flatMap { address =>
      Try {
        requests.post(
          address,
          params = url.params
        )
      }
    }
  }

  override def post(
      uri: Url,
      body: String,
      gzipped: Boolean
  ): Try[Response] = {
    uri.make.flatMap { address =>
      Try {
        val bts              = body.getBytes()
        val (length, entity) = if (gzipped) gzip.compress(bts) else bts.length -> bts

        val headers =
          if (gzipped) {
            List(
              "Content-Length"   -> String.valueOf(length),
              "Content-Encoding" -> "gzip"
            )
          } else Nil

        val request = Request(
          address,
          RequestAuth.Empty,
          params = uri.params,
          headers = headers
        )

        requests.post(
          request,
          // it fails with input stream
          data = RequestBlob.BytesRequestBlob(entity)
        )
      }
    }
  }

  def getStream(url: Url): Try[Iterator[ErrorOr[Array[JArray]]]] = {
    url.make.map { address =>
      var iterator: Iterator[String] = null

      requests.get.stream(
        address,
        params = url.params
      )(onDownload = in => iterator = Source.fromInputStream(in).getLines())

      iterator
        .map(JParser.parseFromStringEither(_))
        .map(
          _.flatMapRight(
            jsonHandler
              .queryResult(_)
              .toRight(new ParsingException("Can't extract query result from response"))
          )
        )
    }
  }
}
