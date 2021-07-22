package com.github.fsanaulla.chronicler.benchmark

import java.util.concurrent.TimeUnit

import com.github.fsanaulla.chronicler.benchmark.InfluxReaderBenchmark._
import com.github.fsanaulla.chronicler.core.alias.ErrorOr
import com.github.fsanaulla.chronicler.core.model.InfluxReader
import com.github.fsanaulla.chronicler.macros.annotations.reader.{epoch, utc}
import com.github.fsanaulla.chronicler.macros.annotations.{field, tag, timestamp}
import com.github.fsanaulla.chronicler.macros.auto._
import org.openjdk.jmh.annotations._
import org.typelevel.jawn.ast._

import scala.util.{Failure, Success, Try}

//[info] Benchmark                                                 Mode  Cnt    Score     Error  Units
//[info] InfluxReaderBenchmark.averageCustomEpochTimestamp         avgt    5   18.454 ±   1.954  ns/op
//[info] InfluxReaderBenchmark.averageCustomEpochTimestampUnsafe   avgt    5   13.594 ±   2.866  ns/op
//[info] InfluxReaderBenchmark.averageEpochTimestamp               avgt    5   17.504 ±   1.915  ns/op
//[info] InfluxReaderBenchmark.averageEpochTimestampUnsafe         avgt    5   12.819 ±   0.445  ns/op
//[info] InfluxReaderBenchmark.averageGeneralEpochTimestamp        avgt    5   22.042 ±   0.605  ns/op
//[info] InfluxReaderBenchmark.averageGeneralEpochTimestampUnsafe  avgt    5   18.809 ±   4.706  ns/op
//[info] InfluxReaderBenchmark.averageGeneralUtcTimestamp          avgt    5  865.616 ±  88.496  ns/op
//[info] InfluxReaderBenchmark.averageGeneralUtcTimestampUnsafe    avgt    5  938.655 ± 165.569  ns/op
//[info] InfluxReaderBenchmark.averageUtcTimestamp                 avgt    5   16.619 ±   1.990  ns/op
//[info] InfluxReaderBenchmark.averageUtcTimestampUnsafe           avgt    5   14.083 ±   0.790  ns/op
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class InfluxReaderBenchmark {

  // custom
  @Benchmark
  def averageCustomEpochTimestamp(state: CustomEpochReader): Unit =
    state.reader.read(JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull)))

  @Benchmark
  def averageCustomEpochTimestampUnsafe(state: CustomEpochReader): Unit =
    state.reader.readUnsafe(
      JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull))
    )

  // safe
  @Benchmark
  def averageEpochTimestamp(state: EpochReader): Unit =
    state.reader.read(JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull)))

  @Benchmark
  def averageUtcTimestamp(state: UtcReader): Unit =
    state.reader.read(
      JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
    )

  @Benchmark
  def averageGeneralEpochTimestamp(state: GeneralEpochReader): Unit =
    state.reader.read(JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull)))

  @Benchmark
  def averageGeneralUtcTimestamp(state: GeneralUtcReader): Unit =
    state.reader.read(
      JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
    )

  // unsafe
  @Benchmark
  def averageEpochTimestampUnsafe(state: EpochReader): Unit =
    state.reader.readUnsafe(
      JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull))
    )

  @Benchmark
  def averageUtcTimestampUnsafe(state: UtcReader): Unit =
    state.reader.readUnsafe(
      JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
    )

  @Benchmark
  def averageGeneralEpochTimestampUnsafe(state: GeneralEpochReader): Unit =
    state.reader.readUnsafe(
      JArray(Array(LongNum(1438715114318570484L), JNum(4), JString("Fz"), JNull))
    )

  @Benchmark
  def averageGeneralUtcTimestampUnsafe(state: GeneralUtcReader): Unit =
    state.reader.readUnsafe(
      JArray(Array(JString("2015-08-04T19:05:14.318570484Z"), JNum(4), JString("Fz"), JNull))
    )

}

object InfluxReaderBenchmark {
  final case class Epoch(
      @tag name: String,
      @tag surname: Option[String],
      @field age: Int,
      @epoch @timestamp time: Long
  )

  final case class Utc(
      @tag name: String,
      @tag surname: Option[String],
      @field age: Int,
      @utc @timestamp time: String
  )

  final case class GeneralEpoch(
      @tag name: String,
      @tag surname: Option[String],
      @field age: Int,
      @timestamp time: Long
  )

  final case class GeneralUtc(
      @tag name: String,
      @tag surname: Option[String],
      @field age: Int,
      @timestamp time: Long
  )

  @State(Scope.Benchmark)
  class EpochReader {
    var reader: InfluxReader[Epoch] = _
    @Setup
    def up(): Unit = reader = InfluxReader[Epoch]
    @TearDown
    def close(): Unit = {}
  }

  @State(Scope.Benchmark)
  class UtcReader {
    var reader: InfluxReader[Utc] = _
    @Setup
    def up(): Unit = reader = InfluxReader[Utc]
    @TearDown
    def close(): Unit = {}
  }

  @State(Scope.Benchmark)
  class GeneralEpochReader {
    var reader: InfluxReader[GeneralEpoch] = _
    @Setup
    def up(): Unit = reader = InfluxReader[GeneralEpoch]
    @TearDown
    def close(): Unit = {}
  }

  @State(Scope.Benchmark)
  class GeneralUtcReader {
    var reader: InfluxReader[GeneralUtc] = _
    @Setup
    def up(): Unit = reader = InfluxReader[GeneralUtc]
    @TearDown
    def close(): Unit = {}
  }

  @State(Scope.Benchmark)
  class CustomEpochReader {
    var reader: InfluxReader[Epoch] = _
    @Setup
    def up(): Unit = reader = new InfluxReader[Epoch] {
      override def read(js: JArray): ErrorOr[Epoch] = {
        val arr = js.vs
        Try(
          Epoch(
            arr(2).asString,
            arr(3).getString,
            arr(1).asInt,
            arr(0).asLong
          )
        ) match {
          case Success(value)     => Right(value)
          case Failure(exception) => Left(exception)
        }
      }

      override def readUnsafe(js: JArray): Epoch = {
        val arr = js.vs
        Epoch(
          arr(2).asString,
          arr(3).getString,
          arr(1).asInt,
          arr(0).asLong
        )
      }
    }
    @TearDown
    def close(): Unit = {}
  }

  @State(Scope.Benchmark)
  class NewEpochReader {
    var reader: InfluxReader[Epoch] = _
    @Setup
    def up(): Unit = reader = InfluxReader[Epoch]
    @TearDown
    def close(): Unit = {}
  }
}
