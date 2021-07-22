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

import java.nio.ByteBuffer

import com.github.fsanaulla.chronicler.core.components.ResponseHandler
import com.github.fsanaulla.chronicler.core.implicits._
import com.github.fsanaulla.chronicler.core.model.ContinuousQuery
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.{DefaultHttpResponse, HttpVersion}
import org.asynchttpclient.Response
import org.asynchttpclient.netty.{EagerResponseBodyPart, NettyResponseStatus}
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Second, Seconds, Span}
import org.typelevel.jawn.ast._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 10.08.17
  */
class AhcResponseHandlerSpec extends AnyFlatSpec with Matchers with ScalaFutures with EitherValues {

  val jsonHandler                 = new AhcJsonHandler()
  implicit val pc: PatienceConfig = PatienceConfig(Span(20, Seconds), Span(1, Second))

  implicit val ex: ExecutionContext = ExecutionContext.Implicits.global

  implicit val timeout: FiniteDuration = 1.second

  implicit val p: JParser.type = JParser

  val rh = new ResponseHandler(jsonHandler)

  def buildResponse(bts: Array[Byte]): Response = {
    val b = new Response.ResponseBuilder()

    b.accumulate(
      new EagerResponseBodyPart(
        Unpooled.copiedBuffer(ByteBuffer.wrap(bts)),
        true
      )
    )

    b.accumulate(
      new NettyResponseStatus(
        null,
        new DefaultHttpResponse(
          HttpVersion.HTTP_1_0,
          io.netty.handler.codec.http.HttpResponseStatus.OK
        ),
        null
      )
    )

    b.build
  }

  it should "extract single query result from response" in {

    val singleResponse =
      """
        |{
        |    "results": [
        |        {
        |            "statement_id": 0,
        |            "series": [
        |                {
        |                    "name": "cpu_load_short",
        |                    "columns": [
        |                        "time",
        |                        "value"
        |                    ],
        |                    "values": [
        |                        [
        |                            "2015-01-29T21:55:43.702900257Z",
        |                            2
        |                        ],
        |                        [
        |                            "2015-01-29T21:55:43.702900257Z",
        |                            0.55
        |                        ],
        |                        [
        |                            "2015-06-11T20:46:02Z",
        |                            0.64
        |                        ]
        |                    ]
        |                }
        |            ]
        |        }
        |    ]
        |}
      """.stripMargin.getBytes()

    val result = Array(
      JArray(Array(JString("2015-01-29T21:55:43.702900257Z"), JNum(2))),
      JArray(Array(JString("2015-01-29T21:55:43.702900257Z"), JNum(0.55))),
      JArray(Array(JString("2015-06-11T20:46:02Z"), JNum(0.64)))
    )

    rh.queryResultJson(buildResponse(singleResponse)).value shouldEqual result
  }

  it should "extract bulk query results from response" in {

    val bulkResponse =
      """
        |{
        |    "results": [
        |        {
        |            "statement_id": 0,
        |            "series": [
        |                {
        |                    "name": "cpu_load_short",
        |                    "columns": [
        |                        "time",
        |                        "value"
        |                    ],
        |                    "values": [
        |                        [
        |                            "2015-01-29T21:55:43.702900257Z",
        |                            2
        |                        ],
        |                        [
        |                            "2015-01-29T21:55:43.702900257Z",
        |                            0.55
        |                        ],
        |                        [
        |                            "2015-06-11T20:46:02Z",
        |                            0.64
        |                        ]
        |                    ]
        |                }
        |            ]
        |        },
        |        {
        |            "statement_id": 1,
        |            "series": [
        |                {
        |                    "name": "cpu_load_short",
        |                    "columns": [
        |                        "time",
        |                        "count"
        |                    ],
        |                    "values": [
        |                        [
        |                            "1970-01-01T00:00:00Z",
        |                            3
        |                        ]
        |                    ]
        |                }
        |            ]
        |        }
        |    ]
        |}
      """.stripMargin.getBytes()

    rh.bulkQueryResultJson(buildResponse(bulkResponse)).value shouldEqual Array(
      Array(
        JArray(Array(JString("2015-01-29T21:55:43.702900257Z"), JNum(2))),
        JArray(Array(JString("2015-01-29T21:55:43.702900257Z"), JNum(0.55))),
        JArray(Array(JString("2015-06-11T20:46:02Z"), JNum(0.64)))
      ),
      Array(
        JArray(Array(JString("1970-01-01T00:00:00Z"), JNum(3)))
      )
    )
  }

  it should "cq unpacking" in {

    val cqStrJson =
      """{
      "results": [
        {
          "statement_id": 0,
          "series": [
            {
              "name": "_internal",
              "columns": [
                "name",
                "query"
              ]
            },
            {
              "name": "my_test_db",
              "columns": [
                "name",
                "query"
              ]
            },
            {
              "name": "mydb",
              "columns": [
                "name",
                "query"
              ],
              "values": [
                [
                  "cq",
                  "CREATE CONTINUOUS QUERY cq ON mydb BEGIN SELECT mean(value) AS mean_value INTO mydb.autogen.aggregate FROM mydb.autogen.cpu_load_short GROUP BY time(30m) END"
                ]
              ]
            },
            {
              "name": "db",
              "columns": [
                "name",
                "query"
              ]
            },
            {
              "name": "fz_db",
              "columns": [
                "name",
                "query"
              ]
            }
          ]
        }
      ]
    }
  """.getBytes()

    val cqi =
      rh.toCqQueryResult(buildResponse(cqStrJson)).value.filter(_.queries.nonEmpty).head
    cqi.dbName shouldEqual "mydb"
    cqi.queries.head shouldEqual ContinuousQuery(
      "cq",
      "CREATE CONTINUOUS QUERY cq ON mydb BEGIN SELECT mean(value) AS mean_value INTO mydb.autogen.aggregate FROM mydb.autogen.cpu_load_short GROUP BY time(30m) END"
    )
  }

  it should "extract optional error message" in {

    val errorResponse =
      """
        |{
        |        "results": [
        |          {
        |            "statement_id": 0,
        |            "error": "user not found"
        |          }
        |        ]
        |}
      """.stripMargin.getBytes()

    jsonHandler.responseErrorMsgOpt(buildResponse(errorResponse)).value shouldEqual Some(
      "user not found"
    )
  }

  it should "extract error message" in {

    val errorResponse =
      """ { "error": "user not found" } """.getBytes()

    jsonHandler
      .responseErrorMsg(buildResponse(errorResponse))
      .value shouldEqual "user not found"
  }
}
