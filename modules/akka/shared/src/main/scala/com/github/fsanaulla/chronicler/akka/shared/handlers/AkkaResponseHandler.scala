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

import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.{Framing, Source}
import akka.util.ByteString
import com.github.fsanaulla.chronicler.akka.shared.implicits._
import com.github.fsanaulla.chronicler.core.alias.ErrorOr
import com.github.fsanaulla.chronicler.core.components.{JsonHandler, ResponseHandler}
import com.github.fsanaulla.chronicler.core.either
import com.github.fsanaulla.chronicler.core.either.EitherOps
import com.github.fsanaulla.chronicler.core.jawn.RichJParser
import com.github.fsanaulla.chronicler.core.model.{InfluxReader, ParsingException}
import org.typelevel.jawn.ast.{JArray, JParser}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class AkkaResponseHandler(
    jsonHandler: JsonHandler[Future, HttpResponse]
)(implicit ex: ExecutionContext)
    extends ResponseHandler[Future, HttpResponse](jsonHandler) {

  final def queryChunkedResultJson(response: HttpResponse): Source[ErrorOr[Array[JArray]], Any] = {
    response.entity.dataBytes
      .via(Framing.delimiter(ByteString("\n"), Int.MaxValue))
      .map(_.utf8String)
      .map(JParser.parseFromStringEither)
      .map(
        _.flatMapRight(jv =>
          jsonHandler
            .queryResult(jv)
            .toRight[Throwable](new ParsingException("Can't extract query result from response"))
        )
      )
  }

  final def queryChunkedResult[T: ClassTag](
      response: HttpResponse
  )(implicit rd: InfluxReader[T]): Source[ErrorOr[Array[T]], Any] = {
    queryChunkedResultJson(response)
      .map(_.flatMapRight { arr =>
        either.array(arr.map(rd.read))
      })
  }
}
