package models.common

import play.api.libs.json._

case class Fee(amount: Seq[Coin], gasLimit: String, payer: String, granter: String)

object Fee {

  implicit val feeReads: Reads[Fee] = Json.reads[Fee]

  implicit val feeWrites: OWrites[Fee] = Json.writes[Fee]

}
