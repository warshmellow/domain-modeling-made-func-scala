package example

case class OrderLineId(value: String)

object OrderLineId {
  def create(fieldName: String, string: String): Either[String, OrderLineId] = {
    ConstrainedType.createString(fieldName, OrderLineId.apply, string)
  }
}
