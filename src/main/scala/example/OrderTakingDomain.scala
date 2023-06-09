package example

import cats.data.{EitherT, NonEmptyList}
import cats.syntax.all._
import com.github.nscala_time.time.Imports._
import example.PlaceOrderWorkflow.PricingError

import scala.concurrent.Future

object OrderTakingDomain {

  type UnitQuantity = Int
  type KilogramQuantity = Double

  type CustomerId = Int

  case class FirstAndLastName(
      firstName: String,
      lastName: String
  )
  case class CustomerInfo(
      name: FirstAndLastName,
      emailAddress: EmailAddress
  )
  case class UnvalidatedCustomerInfo(
      firstName: String,
      lastName: String,
      emailAddress: String
  )
  type ValidatedCustomerInfo = Int
  type Price = Double
  type BillingAmount = Double

  type AddressLine = String
  case class Address(
      addressLine: AddressLine,
      city: String,
      zipCode: String
  )

  case class Order(
      id: OrderId,
      customerId: CustomerId,
      shippingAddress: Address,
      billingAddress: Address,
      orderLines: NonEmptyList[OrderLine],
      amountToBill: BillingAmount
  )

  case class UnvalidatedOrderLine(
      orderLineId: String,
      productCode: String,
      quantity: Double
  )
  case class OrderLine(
      id: OrderLineId,
      orderId: OrderId,
      productCode: ProductCode,
      orderQuantity: OrderQuantity,
      price: Price
  )

  case class UnvalidatedOrder(
      orderId: String,
      customerInfo: UnvalidatedCustomerInfo,
      shippingAddress: UnvalidatedAddress,
      billingAddress: UnvalidatedAddress,
      lines: Seq[UnvalidatedOrderLine]
  )

  case class ValidatedOrder(
      orderId: String,
      customerInfo: String,
      shippingAddress: ValidatedAddress,
      billingAddress: String,
      orderLines: Seq[String],
      amountToBill: Int
  )

  case class PlaceOrderEvents(
      acknowledgementSent: Boolean
  )

  case class ValidationError(error: String)

  sealed trait PlaceOrderError
  case class Validation(validationError: ValidationError)
      extends PlaceOrderError
  case class Pricing(pricingError: PricingError) extends PlaceOrderError

  type PlaceOrder =
    UnvalidatedOrder => EitherT[Future, PlaceOrderError, Seq[PlaceOrderEvent]]

  type VerifiedEmailAddress = String

  sealed trait CustomerEmail
  case class Unverified(emailAddress: EmailAddress) extends CustomerEmail
  case class Verified(verifiedEmailAddress: VerifiedEmailAddress)
      extends CustomerEmail

  type EmailContactInfo = String
  type PostalContactInfo = String
  case class BothContactMethods(
      email: EmailContactInfo,
      address: PostalContactInfo
  )

  sealed trait ContactInfo
  case class EmailOnly(emailContactInfo: EmailContactInfo) extends ContactInfo
  case class AddrOnly(postalContactInfo: PostalContactInfo) extends ContactInfo
  case class EmailAndAddr(bothContactMethods: BothContactMethods)
      extends ContactInfo

  case class Contact(
      name: String,
      contactInfo: ContactInfo
  )

  case class UnvalidatedAddress(
      addressLine: String,
      city: String,
      zipCode: String
  )
  type ValidatedAddress = String
  type AddressValidationService = UnvalidatedAddress => Option[ValidatedAddress]

  case class Command[T](
      data: T,
      timestamp: DateTime,
      userId: String
  )

  type PlaceOrderCommand = Command[UnvalidatedOrder]

  type OrderPlaced = String
  type BillableOrderPlaced = String
  type OrderAcknowledgementSent = String

  sealed trait PlaceOrderEvent
  case class OrderPlacedEvent(orderPlaced: OrderPlaced) extends PlaceOrderEvent
  case class BillableOrderPlacedEvent(billableOrderPlaced: BillableOrderPlaced)
      extends PlaceOrderEvent
  case class AcknowledgementSent(
      orderAcknowledgementSent: OrderAcknowledgementSent
  ) extends PlaceOrderEvent

  type PlaceOrderWorkflow =
    PlaceOrderCommand => EitherT[Future, PlaceOrderError, Seq[PlaceOrderEvent]]

  case class PricedOrderLine(
      orderLineId: OrderLineId,
      productCode: ProductCode,
      quantity: OrderQuantity,
      linePrice: Price
  )

  case class PricedOrder(
      orderId: OrderId,
      customerInfo: CustomerInfo,
      shippingAddress: Address,
      billingAddress: Address,
      amountToBill: BillingAmount,
      lines: Seq[PricedOrderLine]
  )
}
