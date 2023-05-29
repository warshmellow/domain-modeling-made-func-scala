package example

import cats.syntax.all._

sealed trait ProductCode

case class Widget(widgetCode: WidgetCode) extends ProductCode

case class Gizmo(gizmoCode: GizmoCode) extends ProductCode

object ProductCode {
  def create(fieldName: String, code: String): Either[String, ProductCode] = {
    if (code.isBlank) {
      val msg = s"$fieldName: Must not be null or empty"
      msg.asLeft
    } else if (code.startsWith("W")) {
      WidgetCode.create(fieldName, code).map(Widget.apply)
    } else if (code.startsWith("G")) {
      GizmoCode.create(fieldName, code).map(Gizmo.apply)
    } else {
      val msg = s"$fieldName: Format not recognized '$code'"
      msg.asLeft
    }
  }
}

case class WidgetCode(value: String)
object WidgetCode {
  def create(fieldName: String, code: String): Either[String, WidgetCode] = {
    val pattern = "W\\d{4}".r
    ConstrainedType.createLike(fieldName, WidgetCode.apply, pattern, code)
  }
}

case class GizmoCode(value: String)
object GizmoCode {
  def create(fieldName: String, code: String): Either[String, GizmoCode] = {
    val pattern = "G\\d{3}".r
    ConstrainedType.createLike(fieldName, GizmoCode.apply, pattern, code)
  }
}
