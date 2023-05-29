package example

import example.OrderTakingDomain.Price

object Price {
  def create(v: Double) =
    ConstrainedType.createDecimal[Price]("Price", 0, 1000, v)
  def multiply(qty: Double, price: Price) = create(qty * price)

  def multiply(qty: OrderQuantity, price: Price): Either[String, Price] = qty match {
    case Unit(unitQuantity)         => multiply(unitQuantity, price)
    case Kilogram(kilogramQuantity) => multiply(kilogramQuantity, price)
  }
}
