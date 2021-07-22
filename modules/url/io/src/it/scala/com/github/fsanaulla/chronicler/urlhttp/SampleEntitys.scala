package com.github.fsanaulla.chronicler.urlhttp

import com.github.fsanaulla.chronicler.testing.it.FakeEntity
import org.typelevel.jawn.ast.{JArray, JNum, JString}

object SampleEntitys {

  final val currentNanoTime: Long = System.currentTimeMillis() * 1000000

  // INTEGRATION SPEC ENTITYS
  val singleEntity = FakeEntity("Martin", "Odersky", 58)

  val multiEntitys = Array(FakeEntity("Harold", "Lois", 44), FakeEntity("Harry", "Potter", 21))

  val largeMultiJsonEntity = Array(
    Array(
      JArray(
        Array(
          JNum(currentNanoTime),
          JNum(54),
          JString("Martin"),
          JString("Odersky"),
          JString("Male")
        )
      ),
      JArray(
        Array(JNum(currentNanoTime), JNum(36), JString("Jame"), JString("Franko"), JString("Male"))
      ),
      JArray(
        Array(
          JNum(currentNanoTime),
          JNum(54),
          JString("Martin"),
          JString("Odersky"),
          JString("Male")
        )
      )
    ),
    Array(
      JArray(
        Array(JNum(currentNanoTime), JNum(36), JString("Jame"), JString("Franko"), JString("Male"))
      )
    )
  )
}
