package example

import example.OrderTakingDomain.UnitQuantity

class UnitQuantitySpec extends munit.FunSuite {
  test("constructor") {
    val unitQtyResult: Either[String, UnitQuantity] = UnitQuantity.create(1)
    assertEquals(unitQtyResult.toOption.get, 1)
  }
}
