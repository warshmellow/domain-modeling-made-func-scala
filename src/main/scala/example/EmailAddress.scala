package example
import cats.syntax.all._
case class EmailAddress(
    username: String,
    domainName: String
)

object EmailAddress {
  def create(string: String): Option[EmailAddress] = {
    if (string.contains("@")) {
      val splits = string.split("@")
      EmailAddress(
        username = splits(0),
        domainName = splits(1)
      ).some
    } else {
      none
    }
  }
}
