package queries.responses.common

import models.common.Serializable
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utilities.MicroNumber

case class Coin(denom: String, amount: MicroNumber) {
  def toCoin: Serializable.Coin = Serializable.Coin(denom = denom, amount = amount)
}

object Coin {
  def apply(denom: String, amount: String): Coin = new Coin(denom, new MicroNumber(BigDecimal(amount).toBigInt))

  implicit val coinReads: Reads[Coin] = (
    (JsPath \ "denom").read[String] and
      (JsPath \ "amount").read[String]
    ) (apply _)

}
