package example

import cats.data.{EitherT, NonEmptyList}
import cats.syntax.all._
import com.github.nscala_time.time.Imports._

import scala.concurrent.Future

object OrderTakingDomain {
  type WidgetCode = String
  type GizmoCode = String

  sealed trait ProductCode
  case class Widget(widgetCode: WidgetCode) extends ProductCode
  case class Gizmo(gizmoCode: GizmoCode) extends ProductCode

  type UnitQuantity = Int
  type KilogramQuantity = Double

  sealed trait OrderQuantity
  case class Unit(unitQuantity: UnitQuantity)
  case class Kilos(kilogramQuantity: KilogramQuantity)

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
  type Price = String
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
      billingAddress: String,
      orderLines: Seq[String],
      amountToBill: Int
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

  sealed trait PlaceOrderError
  case class ValidationError(
      fieldName: String,
      errorDescription: String
  ) extends PlaceOrderError

  type PlaceOrder =
    UnvalidatedOrder => Either[PlaceOrderError, PlaceOrderEvents]

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

  type UnvalidatedAddress = String
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
}
