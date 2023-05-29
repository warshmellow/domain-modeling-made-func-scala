package example

import example.OrderTakingDomain.{BillingAmount, Price}

object BillingAmount {
  def create(v: Double): Either[String, BillingAmount] =
    ConstrainedType.createDecimal("BillingAmount", 0, 10000, v)

  def sumPrices(prices: Seq[Price]): Either[String, BillingAmount] = {
    create(prices.sum)
  }
}
