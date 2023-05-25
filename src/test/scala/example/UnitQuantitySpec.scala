package example

import example.OrderTakingDomain.UnitQuantity

class UnitQuantitySpec extends munit.FunSuite {
  test("constructor") {
    val unitQtyResult: Either[String, UnitQuantity] = UnitQuantity.create(1)
    unitQtyResult match {
      case Left(value) => ???
      case Right(value) =>
        assertEquals(value, 1)
    }
  }
}
