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

package com.github.fsanaulla.chronicler.urlhttp.shared

import java.net.HttpCookie

import requests.{BaseSession, Compress, RequestAuth}

import scala.collection.mutable

/** Same as requester base session, with small changes in default headers
  */
class ChroniclerSession extends BaseSession {
  def cookies = mutable.Map.empty[String, HttpCookie]

  val headers: Map[String, String] = Map(
    "User-Agent" -> "requests-scala",
//    "Accept-Encoding" -> "gzip, deflate",
    "Connection" -> "keep-alive",
    "Accept"     -> "*/*"
  )

  def auth: RequestAuth.Empty.type = RequestAuth.Empty

  def proxy: Null = null

  def maxRedirects: Int = 5

  def persistCookies = false

  def readTimeout: Int = 10 * 1000

  def connectTimeout: Int = 10 * 1000

  def verifySslCerts: Boolean = true

  def autoDecompress: Boolean = true

  def compress: Compress = Compress.None
}
