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

package com.github.fsanaulla.chronicler.core.model

import com.github.fsanaulla.chronicler.core.implicits.RichString

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 05.08.17
  */
final case class InfluxTag(key: String, value: String) {
  require(value.nonEmpty, "Value can't be empty string")
}

sealed trait InfluxField extends Product with scala.Serializable {
  def key: String
}

final case class StringField(key: String, value: String) extends InfluxField {
  override def toString: String = key.escapeKey + "=" + "\"" + value + "\""
}

final case class IntField(key: String, value: Int) extends InfluxField {
  override def toString: String = key.escapeKey + "=" + value + "i"
}

final case class LongField(key: String, value: Long) extends InfluxField {
  override def toString: String = key.escapeKey + "=" + value
}

final case class DoubleField(key: String, value: Double) extends InfluxField {
  override def toString: String = key.escapeKey + "=" + value
}

final case class BooleanField(key: String, value: Boolean) extends InfluxField {
  override def toString: String = key.escapeKey + "=" + value
}

final case class CharField(key: String, value: Char) extends InfluxField {
  override def toString: String = key.escapeKey + "=" + value
}

final case class BigDecimalField(key: String, value: BigDecimal) extends InfluxField {
  override def toString: String = key.escapeKey + "=" + value
}

final case class Point(
    measurement: String,
    tags: List[InfluxTag] = Nil,
    fields: List[InfluxField] = Nil,
    time: Long = -1L
) {

  def addTag(key: String, value: String): Point = copy(tags = tags :+ InfluxTag(key, value))

  def addField(key: String, value: String): Point = {
    require(value.nonEmpty, "String can't be empty")
    copy(fields = fields :+ StringField(key, value))
  }
  def addField(key: String, value: Int): Point    = copy(fields = fields :+ IntField(key, value))
  def addField(key: String, value: Long): Point   = copy(fields = fields :+ LongField(key, value))
  def addField(key: String, value: Double): Point = copy(fields = fields :+ DoubleField(key, value))
  def addField(key: String, value: Float): Point =
    copy(fields = fields :+ DoubleField(key, value.toDouble))

  def addField(key: String, value: BigDecimal): Point =
    copy(fields = fields :+ BigDecimalField(key, value))

  def addField(key: String, value: Boolean): Point =
    copy(fields = fields :+ BooleanField(key, value))
  def addField(key: String, value: Char): Point = copy(fields = fields :+ CharField(key, value))

  /** You need to specify time in nanosecond precision */
  def addTimestamp(time: Long): Point = {
    require(time >= 0, "Time must be greater that 0.")
    copy(time = time)
  }

  def serialize: String = {
    val sb = new StringBuilder()

    sb.append(measurement.escapeMeas)

    if (tags.nonEmpty) {
      sb.append(
        "," + tags
          .map(tag => tag.key.escapeKey + "=" + tag.value.escapeKey)
          .mkString(",")
      )
    }

    sb.append(" ")
    sb.append(fields.map(_.toString).mkString(","))

    if (time != -1L) {
      sb.append(" ").append(time)
    }

    sb.toString()
  }
}
