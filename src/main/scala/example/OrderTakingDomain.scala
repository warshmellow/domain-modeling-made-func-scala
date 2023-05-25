package example

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
      orderLines: Seq[OrderLine],
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
      shippingAddress: String,
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

}
