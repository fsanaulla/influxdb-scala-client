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

import java.nio.charset.{Charset, StandardCharsets}

import com.github.fsanaulla.chronicler.core.alias.{ErrorOr, Id}
import com.github.fsanaulla.chronicler.core.components.JsonHandler
import com.github.fsanaulla.chronicler.core.encoding.encodingFromContentType
import com.github.fsanaulla.chronicler.core.implicits._
import com.github.fsanaulla.chronicler.core.jawn.RichJParser
import org.asynchttpclient.Response
import org.typelevel.jawn.ast.{JParser, JValue}

import scala.jdk.CollectionConverters._

private[ahc] final class AhcJsonHandler extends JsonHandler[Id, Response] {

  /** *
    * Extract response body
    *
    * @see - [https://groups.google.com/forum/#!searchin/asynchttpclient/compression%7Csort:date/asynchttpclient/TAq33OWXeKU/sBm3v4EWAwAJ],
    *        netty automatically decompress gzipped request
    */
  def responseBody(response: Response): ErrorOr[JValue] = {
    val bodyBts = response.getResponseBodyAsBytes
    val encoding: Charset = Option(response.getContentType)
      .flatMap(encodingFromContentType)
      .map(Charset.forName)
      .getOrElse(StandardCharsets.UTF_8)

    val bodyStr = new String(bodyBts, encoding)

    JParser.parseFromStringEither(bodyStr)
  }

  def responseHeader(response: Response): List[(String, String)] =
    response.getHeaders
      .entries()
      .asScala
      .toList
      .map(e => e.getKey -> e.getValue)

  def responseCode(response: Response): Int =
    response.getStatusCode
}
