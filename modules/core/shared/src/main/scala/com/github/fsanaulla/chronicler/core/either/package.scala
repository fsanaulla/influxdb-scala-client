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

import scala.reflect.ClassTag

package object either {
  def array[L, R: ClassTag](s: Array[Either[L, R]]): Either[L, Array[R]] =
      s.foldRight(Right(Array.empty[R]): Either[L, Array[R]]) {
        (e, acc) =>
          for {
            xs <- acc.right
            x  <- e.right
          } yield x +: xs
      }

//  def fromQueryResult[R: InfluxReader](result: QueryResult[JArray]): Either[Throwable, QueryResult[R]] = {
//    either
//      .array[Throwable, R](result.queryResult.map(implicitly[InfluxReader[R]].read))
//      .map(r => QueryResult.successful(result.code, r))
//  }
}