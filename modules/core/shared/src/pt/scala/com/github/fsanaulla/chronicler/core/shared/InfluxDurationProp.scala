package com.github.fsanaulla.chronicler.core.shared

import com.github.fsanaulla.chronicler.core.duration._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatestplus.scalacheck.Checkers

class InfluxDurationProp extends AnyFlatSpec with Checkers {

  def toInfluxDuration[@specialized(Int, Long) T](w: T, d: T, h: T, m: T, s: T) =
    s"${w}w${d}d${h}h${m}m${s}s"

  "InfluxDuration" should "build duration from Int" in check {
    (w: Int, d: Int, h: Int, m: Int, s: Int) =>
      val implVal = w.weeks + d.days + h.hours + m.minutes + s.seconds
      val expVal  = toInfluxDuration(w, d, h, m, s)

      implVal == expVal
  }

  it should "build duration from Long" in check { (w: Long, d: Long, h: Long, m: Long, s: Long) =>
    val implVal = w.weeks + d.days + h.hours + m.minutes + s.seconds
    val expVal  = toInfluxDuration(w, d, h, m, s)

    implVal == expVal
  }
}
