package example

import cats.data.EitherT
import example.OrderTakingDomain._
import cats.syntax.all._

import scala.concurrent.Future

object PlaceOrderWorkflow {
  type ValidatedOrderLine = String
  case class ValidatedOrder(
      orderId: OrderId,
      customerInfo: CustomerInfo,
      shippingAddress: ShippingAddress,
      billingAddress: BillingAddress,
      orderLines: Seq[ValidatedOrderLine]
  )

  type PricedOrder = String

  sealed trait Order
  case class Unvalidated(unvalidatedOrder: UnvalidatedOrder) extends Order
  case class Validated(validatedOrder: ValidatedOrder) extends Order
  case class Priced(pricedOrder: PricedOrder) extends Order

  type CheckProductCodeExists = ProductCode => Boolean

  type AddressValidationError = IllegalArgumentException
  type CheckedAddress = ValidatedAddress
  type CheckAddressExists = UnvalidatedAddress => EitherT[
    Future,
    AddressValidationError,
    CheckedAddress
  ]

  type ValidateOrder =
    CheckProductCodeExists => UnvalidatedOrder => EitherT[Future, Seq[
      ValidatedOrder
    ], ValidatedOrder]

  type GetProductPrice = ProductCode => Price
  type PricingError = IllegalArgumentException
  type PriceOrder =
    GetProductPrice => ValidatedOrder => Either[PricingError, PricedOrder]
}
