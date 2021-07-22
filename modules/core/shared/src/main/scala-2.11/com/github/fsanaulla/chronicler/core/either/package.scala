package com.github.fsanaulla.chronicler.core

import scala.reflect.ClassTag

package object either {

  def array[L, R: ClassTag](s: Array[Either[L, R]]): Either[L, Array[R]] =
    s.foldRight(Right(Array.empty[R]): Either[L, Array[R]]) { (e, acc) =>
      for {
        xs <- acc.right
        x  <- e.right
      } yield x +: xs
    }

  def seq[L, R](s: Seq[Either[L, R]]): Either[L, Seq[R]] =
    s.foldRight(Right(Seq.empty): Either[L, Seq[R]]) { (e, acc) =>
      for {
        xs <- acc.right
        x  <- e.right
      } yield x +: xs
    }

  // to be back compatible with scala 2.11
  implicit final class EitherOps[A, B](private val either: Either[A, B]) extends AnyVal {

    def mapRight[C](f: B => C): Either[A, C] =
      either.right.map(f)

    def mapLeft[C](f: A => C): Either[C, B] =
      either.left.map(f)

    def flatMapRight[C](f: B => Either[A, C]): Either[A, C] =
      either.right.flatMap(f)

    def flatMapLeft[C](f: A => Either[C, B]): Either[C, B] =
      either.left.flatMap(f)

    def getOrElseRight[B1 >: B](or: => B1): B1 =
      either.right.getOrElse(or)
  }
}
