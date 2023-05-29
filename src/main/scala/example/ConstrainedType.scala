package example

import cats.syntax.all._

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
}
