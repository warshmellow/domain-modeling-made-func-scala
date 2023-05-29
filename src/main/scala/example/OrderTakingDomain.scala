package example

import cats.data.NonEmptyList
import cats.syntax.all._
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

  type OrderId = Int
  type OrderLineId = Int
  type CustomerId = Int

  type CustomerInfo = Int
  type ShippingAddress = String
  type BillingAddress = String
  type Price = String
  type BillingAmount = Double

  case class Order(
      id: OrderId,
      customerId: CustomerId,
      shippingAddress: ShippingAddress,
      billingAddress: BillingAddress,
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
      customerInfo: String,
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

  type EmailAddress = String
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
}
