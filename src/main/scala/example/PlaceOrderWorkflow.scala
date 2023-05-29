package example

import cats.data.EitherT
import example.OrderTakingDomain.{UnvalidatedOrder, _}
import cats.syntax.all._
import example.PlaceOrderWorkflow.{
  CheckAddressExists,
  CheckProductCodeExists,
  toOrderLineId
}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object PlaceOrderWorkflow {
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
      ValidationError,
      ValidatedOrder
    ]

  type GetProductPrice = ProductCode => Price
  case class PricingError(str: String)
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

  // Implementation
  def toCustomerInfo(
      unvalidatedCustomerInfo: UnvalidatedCustomerInfo
  ): Either[ValidationError, CustomerInfo] = {
    EmailAddress.create(unvalidatedCustomerInfo.emailAddress) match {
      case Some(emailAddress) =>
        CustomerInfo(
          name = FirstAndLastName(
            unvalidatedCustomerInfo.firstName,
            unvalidatedCustomerInfo.lastName
          ),
          emailAddress
        ).asRight
      case None =>
        ValidationError("emailAddress invalid").asLeft
    }

  }

  def toAddress(
      checkedAddress: CheckedAddress
  ): Either[ValidationError, Address] = {

    val address = checkedAddress.unvalidatedAddress

    Address(
      addressLine = address.addressLine,
      city = address.city,
      zipCode = address.zipCode
    ).asRight
  }

  def toCheckedAddress(checkAddress: CheckAddressExists)(
      unvalidatedAddress: UnvalidatedAddress
  ): EitherT[
    Future,
    ValidationError,
    CheckedAddress
  ] = {
    val result =
      checkAddress(unvalidatedAddress).value.map {
        case Left(e) =>
          e match {
            case InvalidFormat =>
              ValidationError("Address has bad format").asLeft
            case AddressNotFound =>
              ValidationError("Address not found").asLeft
          }
        case Right(value) => value.asRight
      }
    EitherT(result)
  }

  def toOrderId(orderId: String): Either[ValidationError, example.OrderId] = {
    OrderId.create("OrderId", orderId) match {
      case Left(errorMsg) =>
        ValidationError(
          errorMsg
        ).asLeft
      case Right(value) => value.asRight
    }
  }

  def toOrderLineId(
      orderId: String
  ): Either[ValidationError, OrderLineId] = {
    OrderLineId.create("OrderLineId", orderId) match {
      case Left(error) =>
        ValidationError(
          error
        ).asLeft
      case Right(value) => value.asRight
    }
  }

  def toProductCode(checkProductCodeExists: CheckProductCodeExists)(
      productCode: String
  ): Either[ValidationError, ProductCode] = {
    def checkProduct(
        productCode: ProductCode
    ): Either[ValidationError, ProductCode] = {
      if (checkProductCodeExists(productCode)) {
        productCode.asRight
      } else {
        val msg = s"Invalid: $productCode"
        ValidationError(msg).asLeft
      }
    }

    ProductCode.create("ProductCode", productCode) match {
      case Left(error)        => ValidationError(error).asLeft
      case Right(productCode) => checkProduct(productCode)
    }
  }

  def toOrderQuantity(
      productCode: ProductCode,
      quantity: Double
  ): Either[ValidationError, OrderQuantity] = {
    OrderQuantity.create("OrderQuantity", productCode, quantity) match {
      case Left(error)  => ValidationError(error).asLeft
      case Right(value) => value.asRight
    }
  }

  def toValidatedOrderLine(checkProductExists: CheckProductCodeExists)(
      unvalidatedOrderLine: UnvalidatedOrderLine
  ): Either[ValidationError, ValidatedOrderLine] = {
    for {
      orderLineId <- toOrderLineId(
        unvalidatedOrderLine.orderLineId
      )
      productCode <- toProductCode(checkProductExists)(
        unvalidatedOrderLine.productCode
      )
      quantity <- toOrderQuantity(
        productCode,
        unvalidatedOrderLine.quantity
      )
    } yield ValidatedOrderLine(
      orderLineId,
      productCode,
      quantity
    )
  }

  def validateOrder: ValidateOrder =
    (checkProductCodeExists: CheckProductCodeExists) =>
      (checkAddressExists: CheckAddressExists) =>
        (unvalidatedOrder: UnvalidatedOrder) => {

          for {
            orderId <- EitherT.fromEither[Future](
              toOrderId(unvalidatedOrder.orderId)
            )
            customerInfo <- EitherT.fromEither[Future](
              toCustomerInfo(unvalidatedOrder.customerInfo)
            )

            checkedShippingAddress <- toCheckedAddress(checkAddressExists)(
              unvalidatedOrder.shippingAddress
            )

            shippingAddress <- EitherT.fromEither[Future](
              toAddress(checkedShippingAddress)
            )

            checkedBillingAddress <-
              toCheckedAddress(checkAddressExists)(
                unvalidatedOrder.billingAddress
              )

            billingAddress <- EitherT.fromEither[Future](
              toAddress(checkedBillingAddress)
            )

            lines <- EitherT.fromEither[Future] {
              unvalidatedOrder.lines
                .map(
                  toValidatedOrderLine(checkProductCodeExists)
                )
                .sequence
            }

          } yield ValidatedOrder(
            orderId = orderId,
            customerInfo = customerInfo,
            shippingAddress = shippingAddress,
            billingAddress = billingAddress,
            lines = lines
          )
        }

  def toPricedOrderLine(
      getProductPrice: GetProductPrice
  )(
      validatedOrderLine: ValidatedOrderLine
  ): Either[PricingError, PricedOrderLine] = {
    val qty: OrderQuantity = validatedOrderLine.quantity
    val price = getProductPrice(validatedOrderLine.productCode)
    Price.multiply(qty, price) match {
      case Left(e) => PricingError(e).asLeft
      case Right(linePrice) =>
        PricedOrderLine(
          validatedOrderLine.orderLineId,
          validatedOrderLine.productCode,
          validatedOrderLine.quantity,
          linePrice
        ).asRight
    }
  }

  /*
  type PriceOrder =
    GetProductPrice => ValidatedOrder => Either[PricingError, PricedOrder]

    case class PricedOrder(
      orderId: OrderId,
      customerInfo: CustomerInfo,
      shippingAddress: Address,
      billingAddress: Address,
      amountToBill: BillingAmount,
      lines: Seq[PricedOrderLine]
  )
   */
  def priceOrder: PriceOrder = (getProductPrice: GetProductPrice) =>
    (validatedOrder: ValidatedOrder) => {
      for {
        lines <- validatedOrder.lines
          .map(toPricedOrderLine(getProductPrice))
          .sequence

        amountToBill <- {
          BillingAmount.sumPrices(lines.map(_.linePrice)) match {
            case Left(e)                             => PricingError(e).asLeft
            case Right(billingAmount: BillingAmount) => billingAmount.asRight
          }
        }
      } yield PricedOrder(
        validatedOrder.orderId,
        validatedOrder.customerInfo,
        validatedOrder.shippingAddress,
        validatedOrder.billingAddress,
        amountToBill,
        lines
      )
    }
}
