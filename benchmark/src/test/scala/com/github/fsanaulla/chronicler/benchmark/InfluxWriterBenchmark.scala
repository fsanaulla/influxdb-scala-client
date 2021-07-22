package com.github.fsanaulla.chronicler.benchmark

import java.util.concurrent.TimeUnit

import com.github.fsanaulla.chronicler.benchmark.InfluxWriterBenchmark._
import com.github.fsanaulla.chronicler.core.model.InfluxWriter
import com.github.fsanaulla.chronicler.macros.Influx
import com.github.fsanaulla.chronicler.macros.annotations.{field, tag, timestamp}
import org.openjdk.jmh.annotations._

//[info] InfluxWriterBenchmark.averageCustomWriteTime  avgt    5   86.526 ±  0.460  ns/op
//[info] InfluxWriterBenchmark.averageMacroWriteTime   avgt    5  156.521 ± 44.614  ns/op
@BenchmarkMode(Array(Mode.All))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class InfluxWriterBenchmark {

  @Benchmark
  def averageCustomWriteTime(state: CustomWriter): Unit =
    state.writer.write(Test("a", Some("b"), 5, 150L))

  @Benchmark
  def averageMacroWriteTime(state: MacroWriter): Unit =
    state.writer.write(Test("a", Some("b"), 5, 150L))
}

object InfluxWriterBenchmark {
  final case class Test(
      @tag name: String,
      @tag surname: Option[String],
      @field age: Int,
      @timestamp time: Long
  )

  @State(Scope.Benchmark)
  class CustomWriter {
    var writer: InfluxWriter[Test] = _

    @Setup
    def up(): Unit =
      writer = (obj: Test) => {
        val sb = new StringBuilder
        sb
          .append("name=")
          .append(obj.name)

        if (obj.surname.isDefined) {
          sb
            .append(",")
            .append("surname=")
            .append(obj.surname.get)
        } else sb.append(" ")

        sb
          .append("age=")
          .append(obj.age)
          .append(" ")
          .append(obj.time)

        Right(sb.toString())
      }

    @TearDown
    def close(): Unit = {}
  }

  @State(Scope.Benchmark)
  class MacroWriter {
    var writer: InfluxWriter[Test] = _
    @Setup
    def up(): Unit = writer = Influx.writer[Test]
    @TearDown
    def close(): Unit = {}
  }
}
