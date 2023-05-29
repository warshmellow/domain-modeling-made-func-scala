package example

import cats.syntax.all._
import example.OrderTakingDomain.{KilogramQuantity, UnitQuantity}

sealed trait OrderQuantity
case class Unit(unitQuantity: UnitQuantity) extends OrderQuantity
case class Kilogram(kilogramQuantity: KilogramQuantity) extends OrderQuantity

object OrderQuantity {
  def create(
      fieldName: String,
      productCode: ProductCode,
      quantity: Double
  ): Either[String, OrderQuantity] = {
    productCode match {
      case Widget(widgetCode) =>
        UnitQuantity.create(fieldName, quantity.toInt).map(Unit.apply)
      case Gizmo(gizmoCode) =>
        KilogramQuantity.create(fieldName, quantity).map(Kilogram.apply)
    }
  }
}

object UnitQuantity {
  def create(fieldName: String, v: Int): Either[String, UnitQuantity] = {
    ConstrainedType.createInt(
      fieldName,
      1,
      1000,
      v
    )
  }
}

object KilogramQuantity {
  def create(fieldName: String, v: Double): Either[String, KilogramQuantity] = {
    ConstrainedType.createDecimal(fieldName, 0.05, 100, v)
  }
}
