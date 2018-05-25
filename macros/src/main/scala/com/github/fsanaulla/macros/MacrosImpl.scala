package com.github.fsanaulla.macros

import com.github.fsanaulla.macros.annotations.{field, tag, timestamp}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/**
  * Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 13.02.18
  */
private[macros] class MacrosImpl(val c: blackbox.Context) {
  import c.universe._

  final val TIMESTAMP_TYPE = tpdls[Long]

  final val SUPPORTED_TAGS_TYPES =
    Seq(tpdls[Option[String]], tpdls[String])

  final val SUPPORTED_FIELD_TYPES =
    Seq(tpdls[Boolean], tpdls[Int], tpdls[Double], tpdls[String], TIMESTAMP_TYPE)

  /** return type dealias */
  def tpdls[A: TypeTag]: c.universe.Type = typeOf[A].dealias

  /** Check if this method valid timestamp */
  def isTimestamp(m: MethodSymbol): Boolean = {
    if (m.annotations.exists(_.tree.tpe =:= typeOf[timestamp])) {
      if (m.returnType =:= TIMESTAMP_TYPE) true
      else c.abort(c.enclosingPosition, s"@timestamp ${m.name} has unsupported type ${m.returnType}. Timestamp must be Long")
    } else false
  }

  /**
    * Generate read method for specified type
    * @param tpe  - for which type
    * @return     - AST that will be expanded to read method
    */
  def createReadMethod(tpe: c.universe.Type): c.universe.Tree = {

    val (ts, othFields) = tpe.decls.toList
      .collect { case m: MethodSymbol if m.isCaseAccessor => m }
      .partition(isTimestamp)

    if (ts.size > 1)
      c.abort(c.enclosingPosition, "Only one field can be marked as @timestamp.")

    if (othFields.lengthCompare(1) < 0)
      c.abort(c.enclosingPosition, "Type parameter must be a case class with more then 1 fields.")

    val fields = othFields.map(m => m.name.decodedName.toString -> m.returnType.dealias)

    val timestamp = ts
      .headOption
      .map(m => TermName(m.name.decodedName.toString) -> m.returnType.dealias)
      .map {case (k, _) => q"$k = $k.asBoolean"}

    val bool = tpdls[Boolean]
    val int = tpdls[Int]
    val long = tpdls[Long]
    val double = tpdls[Double]
    val string = tpdls[String]
    val optString = tpdls[Option[String]]

    val params = fields
      .sortBy(_._1)
      .map { case (k, v) => TermName(k) -> v }
      .map {
        case (k, `bool`) => q"$k = $k.asBoolean"
        case (k, `string`) => q"$k = $k.asString"
        case (k, `int`) => q"$k = $k.asInt"
        case (k, `long`) => q"$k = $k.asLong"
        case (k, `double`) => q"$k = $k.asDouble"
        case (k, `optString`) => q"$k = if ($k.isNull) None else $k.getString"
        case (_, other) => c.abort(c.enclosingPosition, s"Unsupported type $other")
      }

    val paramss: List[Tree] = fields
      .map(_._1)
      .sorted // influx return results in alphabetical order
      .map(k => TermName(k))
      .map(k => pq"$k: JValue")

    val tss: Option[c.universe.Tree] = ts.headOption.map(t => pq"${TermName(t)}: JValue")

    // success case clause component
    val successPat = pq"Array(..${if (tss.isDefined) tss.get :: paramss else paramss})"
    val successBody = q"new $tpe(..${if (timestamp.isDefined) timestamp.get :: params else params})"
    val successCase = cq"$successPat => $successBody"

    // failure case clause component
    val failurePat = pq"_"
    val failureMsg = s"Can't deserialize $tpe object"
    val failureBody = q"throw new DeserializationException($failureMsg)"
    val failureCase = cq"$failurePat => $failureBody"

    val cases = successCase :: failureCase :: Nil

    q"""def read(js: JArray): $tpe =
        ${if (tss.isDefined) q"js.vs match { case ..$cases }" else q"js.vs.tail match { case ..$cases }"}"""
  }

  def createWriteMethod(tpe: c.Type): c.universe.Tree = {
    val methods: List[MethodSymbol] = tpe.decls.toList collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }

    if (methods.lengthCompare(1) < 0)
      c.abort(c.enclosingPosition, "Type parameter must be a case class with more then 1 fields")

    /** Is it Option container*/
    def isOption(tpe: c.universe.Type): Boolean =
      tpe.typeConstructor =:= typeOf[Option[_]].typeConstructor

    def isSupportedTagType(tpe: c.universe.Type): Boolean =
      SUPPORTED_TAGS_TYPES.exists(t => t =:= tpe)

    def isSupportedFieldType(tpe: c.universe.Type): Boolean =
      SUPPORTED_FIELD_TYPES.exists(t => t =:= tpe)

    /** Predicate for finding fields of instance marked with '@tag' annotation */
    def isTag(m: MethodSymbol): Boolean = {
      if (m.annotations.exists(_.tree.tpe =:= typeOf[tag])) {
        if (isSupportedTagType(m.returnType)) true
        else c.abort(c.enclosingPosition, s"@tag ${m.name} has unsupported type ${m.returnType}. Tag must have String or Optional[String]")
      } else false
    }

    /** Predicate for finding fields of instance marked with '@field' annotation */
    def isField(m: MethodSymbol): Boolean = {
      if (m.annotations.exists(_.tree.tpe =:= typeOf[field])) {
        if (isSupportedFieldType(m.returnType)) true
        else c.abort(c.enclosingPosition, s"Unsupported type for @field ${m.name}: ${m.returnType}")
      } else false
    }

    def isMarked(m: MethodSymbol): Boolean = isTag(m) || isField(m)

    val (tagsMethods, fieldsMethods) = methods
      .filter(isMarked)
      .span {
        case m: MethodSymbol if isTag(m) => true
        case _ => false
      }

    val optTags: List[c.universe.Tree] = tagsMethods collect {
      case m: MethodSymbol if isOption(m.returnType) =>
        q"${m.name.decodedName.toString} -> obj.${m.name}"
    }

    val nonOptTags: List[c.universe.Tree] = tagsMethods collect {
      case m: MethodSymbol if !isOption(m.returnType) =>
        q"${m.name.decodedName.toString} -> obj.${m.name}"
    }

    val fields = fieldsMethods map {
      m: MethodSymbol =>
        q"${m.name.decodedName.toString} -> obj.${m.name}"
    }


    q"""def write(obj: $tpe): String = {
            val fieldsMap: Map[String, Any] = Map(..$fields)
            val fields = fieldsMap map { case (k, v) => k + "=" + v } mkString(" ")

            val nonOptTagsMap: Map[String, String] = Map(..$nonOptTags)
            val nonOptTags: String = nonOptTagsMap map {
              case (k: String, v: String) => k + "=" + v
            } mkString(",")

            val optTagsMap: Map[String, Option[String]] = Map(..$optTags)
            val optTags: String = optTagsMap collect {
                case (k: String, v: Option[String]) if v.isDefined => k + "=" + v.get
            } mkString(",")

            val combTags: String = if (optTags.isEmpty) nonOptTags else nonOptTags + "," + optTags

            combTags + " " + fields trim
          }"""
  }

  /***
    * Generate AST for current type at compile time.
    * @tparam T - Type parameter for whom will be generated AST
    */
  def writer_impl[T: c.WeakTypeTag]: c.universe.Tree = {
    val tpe = c.weakTypeOf[T]
    q"""new InfluxWriter[$tpe] {${createWriteMethod(tpe)}} """
  }

  /***
    * Generate AST for current type at compile time.
    * @tparam T - Type parameter for whom will be generated AST
    */
  def reader_impl[T: c.WeakTypeTag]: c.universe.Tree = {
    val tpe = c.weakTypeOf[T]

    q"""new InfluxReader[$tpe] {
          import jawn.ast.{JValue, JArray}
          import com.github.fsanaulla.core.model.DeserializationException

          ${createReadMethod(tpe)}
       }"""
  }

  /***
    * Generate AST for current type at compile time.
    * @tparam T - Type parameter for whom will be generated AST
    */
  def format_impl[T: c.WeakTypeTag]: c.universe.Tree = {
    val tpe = c.weakTypeOf[T]

    q"""
       new InfluxFormatter[$tpe] {
          import jawn.ast.{JValue, JArray}
          import com.github.fsanaulla.core.model.DeserializationException

          ${createWriteMethod(tpe)}
          ${createReadMethod(tpe)}
       }"""
  }
}
