package example

import cats.data.EitherT
import example.OrderTakingDomain._
import cats.syntax.all._

import scala.concurrent.Future

object PlaceOrderWorkflow {
  type PricedOrder = String

  sealed trait Order
  case class Unvalidated(unvalidatedOrder: UnvalidatedOrder) extends Order
  case class Validated(validatedOrder: ValidatedOrder) extends Order
  case class Priced(pricedOrder: PricedOrder) extends Order

  type CheckProductCodeExists = ProductCode => Boolean

  sealed trait AddressValidationError
  case object InvalidFormat extends AddressValidationError
  case object AddressNotFound extends AddressValidationError

  case class CheckedAddress(unvalidatedAddress: UnvalidatedAddress)

  type CheckAddressExists = UnvalidatedAddress => EitherT[
    Future,
    AddressValidationError,
    CheckedAddress
  ]

  case class ValidatedOrderLine(
      orderLineId: OrderLineId,
      productCode: ProductCode,
      quantity: OrderQuantity
  )

  case class ValidatedOrder(
      orderId: OrderId,
      customerInfo: CustomerInfo,
      shippingAddress: Address,
      billingAddress: Address,
      lines: Seq[ValidatedOrderLine]
  )

  type ValidateOrder =
    CheckProductCodeExists => CheckAddressExists => UnvalidatedOrder => EitherT[
      Future,
      Seq[
        ValidatedOrder
      ],
      ValidatedOrder
    ]

  type GetProductPrice = ProductCode => Price
  type PricingError = IllegalArgumentException
  type PriceOrder =
    GetProductPrice => ValidatedOrder => Either[PricingError, PricedOrder]

  case class HtmlString(string: String)

  case class OrderAcknowledgment(
      emailAddress: EmailAddress,
      letter: HtmlString
  )

  type CreateOrderAcknowledgmentLetter = PricedOrder => HtmlString

  sealed trait SendResult
  case object Sent extends SendResult
  case object NotSent extends SendResult

  type SendOrderAcknowledgment = OrderAcknowledgment => SendResult

  type AcknowledgeOrder =
    CreateOrderAcknowledgmentLetter => SendOrderAcknowledgment => PricedOrder => Option[
      OrderAcknowledgment
    ]

  type CreateEvents =
    PricedOrder => Option[OrderAcknowledgment] => Seq[PlaceOrderEvent]
}
