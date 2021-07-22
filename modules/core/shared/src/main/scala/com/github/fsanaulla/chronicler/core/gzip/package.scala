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

package com.github.fsanaulla.chronicler.core

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

package object gzip {

  /** *
    * Compress the data, and return compressed byte array and it length
    *
    * @since - 0.5.5
    */
  def compress(data: Array[Byte]): (Int, Array[Byte]) = {
    val bArrOut    = new ByteArrayOutputStream()
    val gzippedOut = new GZIPOutputStream(bArrOut)

    gzippedOut.write(data)
    gzippedOut.close()

    val gzippedData   = bArrOut.toByteArray
    val contentLength = gzippedData.length
    contentLength -> gzippedData
  }

  def decompress(data: Array[Byte]): Array[Byte] = {
    val gis = new GZIPInputStream(new ByteArrayInputStream(data))
    val out = new ByteArrayOutputStream()
    val buf = new Array[Byte](1024)

    var res = 0
    while (res >= 0) {
      res = gis.read(buf, 0, buf.length)
      if (res > 0) out.write(buf, 0, res)
    }

    out.toByteArray
  }
}
