package example

import cats.syntax.all._

import scala.util.matching.Regex

object ConstrainedType {
  def createString[T](
      fieldName: String,
      ctor: String => T,
      string: String
  ): Either[String, T] = {
    if (string.isBlank) {
      val msg = s"$fieldName must not be null or empty"
      msg.asLeft
    } else {
      ctor(string).asRight
    }
  }

  def createInt[T](
      fieldName: String,
      minVal: Int,
      maxVal: Int,
      i: Int
  ): Either[String, T] = {
    if (i < minVal) {
      val msg = s"$fieldName: Must not be less than $minVal"
      msg.asLeft
    } else if (i > maxVal) {
      val msg = s"$fieldName: Must not be greater than $maxVal"
      msg.asLeft
    } else {
      i.asInstanceOf[T].asRight
    }
  }

  def createDecimal[T](
      fieldName: String,
      minVal: Double,
      maxVal: Double,
      i: Double
  ): Either[String, T] = {
    if (i < minVal) {
      val msg = s"$fieldName: Must not be less than $minVal"
      msg.asLeft
    } else if (i > maxVal) {
      val msg = s"$fieldName: Must not be greater than $maxVal"
      msg.asLeft
    } else {
      i.asInstanceOf[T].asRight
    }
  }

  def createLike[T](
      fieldName: String,
      ctor: String => T,
      pattern: Regex,
      string: String
  ): Either[String, T] = {
    if (string.isBlank) {
      val msg = s"$fieldName: Must not be null or empty"
      msg.asLeft
    } else if (pattern.matches(string)) {
      ctor(string).asRight
    } else {
      val msg = s"$fieldName: '$string' must match the pattern '$pattern'"
      msg.asLeft
    }
  }
}
