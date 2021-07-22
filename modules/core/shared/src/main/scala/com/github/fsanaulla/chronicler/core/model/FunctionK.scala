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

import com.github.fsanaulla.chronicler.core.alias.Id

/** A FunctionK transforms values from one first-order-kinded type (a type that takes a single type parameter,
  * such as List or Option) into another first-order-kinded type.
  * This transformation is universal, meaning that a FunctionK[List, Option] will translate all List[A] values into an Option[A]
  * value for all possible types of A. This explanation may be easier to understand if we first step back and talk about ordinary functions.
  *
  * @see - https://typelevel.org/cats/datatypes/functionk.html
  *
  * @tparam F - result effect
  * @tparam G - incoming effect
  *
  * @since 0.5.5
  */
trait FunctionK[G[_], F[_]] {
  def apply[A](fa: G[A]): F[A]
}

object FunctionK {
  implicit def id[F[_]](implicit A: Apply[F]): FunctionK[Id, F] = new FunctionK[Id, F] {
    override def apply[A](fa: Id[A]): F[A] = A.pure(fa)
  }

  implicit def identity[F[_]]: FunctionK[F, F] = new FunctionK[F, F] {
    override def apply[A](fa: F[A]): F[A] = fa
  }
}
