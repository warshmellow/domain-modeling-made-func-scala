package example

import cats.syntax.all._

case class OrderId(value: String)
object OrderId {
  def create(fieldName: String, str: String): Either[String, OrderId] = {
    if (str.isBlank) {
      val msg = s"$fieldName must not be null or empty"
      msg.asLeft
    } else {
      OrderId(str).asRight
    }
  }
}
