package example

import example.OrderTakingDomain.UnitQuantity

object UnitQuantity {
  def create(qty: Int): Either[String, UnitQuantity] = {
    if (qty < 1) {
      Left("UnitQuantity can not be negative")
    } else if (qty > 1000) {
      Left("UnitQuantity can not be more than 1000")
    } else {
      Right(qty)
    }
  }
}
