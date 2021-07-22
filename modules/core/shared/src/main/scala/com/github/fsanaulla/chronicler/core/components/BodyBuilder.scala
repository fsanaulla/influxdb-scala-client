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

package com.github.fsanaulla.chronicler.core.components

import java.nio.file.Path

import com.github.fsanaulla.chronicler.core.alias.ErrorOr
import com.github.fsanaulla.chronicler.core.either
import com.github.fsanaulla.chronicler.core.either.EitherOps
import com.github.fsanaulla.chronicler.core.model.{Appender, InfluxWriter, Point}

import scala.io.Source

trait BodyBuilder[A] {
  def fromFile(filePath: Path, enc: String): A
  def fromString(string: String): A
  def fromStrings(strings: Seq[String]): A
  def fromPoint(point: Point): A
  def fromPoints(points: Seq[Point]): A
  def fromT[T](meas: String, t: T)(implicit wr: InfluxWriter[T]): ErrorOr[A]
  def fromSeqT[T](meas: String, ts: Seq[T])(implicit wr: InfluxWriter[T]): ErrorOr[A]
}

object BodyBuilder {
  implicit val stringBodyBuilder: BodyBuilder[String] = new BodyBuilder[String] with Appender {
    override def fromFile(filePath: Path, enc: String): String =
      Source
        .fromFile(filePath.toUri, enc)
        .getLines()
        .mkString("\n")

    override def fromStrings(strings: Seq[String]): String =
      strings.mkString("\n")

    override def fromPoint(point: Point): String =
      point.serialize

    override def fromPoints(points: Seq[Point]): String =
      points.map(_.serialize).mkString("\n")

    override def fromString(string: String): String =
      string

    override def fromT[T](meas: String, t: T)(implicit wr: InfluxWriter[T]): ErrorOr[String] =
      wr.write(t).mapRight(append(meas, _))

    override def fromSeqT[T](
        meas: String,
        ts: Seq[T]
    )(implicit wr: InfluxWriter[T]): ErrorOr[String] = {
      either.seq(ts.map(wr.write)).mapRight(append(meas, _))
    }
  }
}
