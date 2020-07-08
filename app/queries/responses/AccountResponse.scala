package queries.responses

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}
import play.api.libs.json.{Json, OWrites, Reads}
import transactions.Abstract.BaseResponse
import utilities.MicroNumber

object AccountResponse {

  case class Coin(denom: String, amount: MicroNumber)

  object Coin {
    def apply(denom: String, amount: String): Coin = new Coin(denom, new MicroNumber(BigInt(amount)))

    def unapply(arg: Coin): Option[(String, String)] = Option(arg.denom, arg.amount.toMicroString)
  }

  implicit val coinReads: Reads[Coin] = (
    (JsPath \ "denom").read[String] and
      (JsPath \ "amount").read[String]
    ) (Coin.apply _)

  implicit val coinsWrites: OWrites[Coin] = Json.writes[Coin]

  case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: MicroNumber, assetPrice: MicroNumber, quantityUnit: String, ownerAddress: String, locked: Boolean, moderated: Boolean, takerAddress: String)

  object Asset {
    def apply(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, ownerAddress: String, locked: Boolean, moderated: Boolean, takerAddress: String): Asset = new Asset(pegHash, documentHash, assetType, new MicroNumber(BigInt(assetQuantity)), new MicroNumber(BigInt(assetPrice)), quantityUnit, ownerAddress, locked, moderated, takerAddress)

    def unapply(arg: Asset): Option[(String, String, String, String, String, String, String, Boolean, Boolean, String)] = Option(arg.pegHash, arg.documentHash, arg.assetType, arg.assetQuantity.toMicroString, arg.assetPrice.toMicroString, arg.quantityUnit, arg.ownerAddress, arg.locked, arg.moderated, arg.takerAddress)
  }

  implicit val assetReads: Reads[Asset] = (
    (JsPath \ "pegHash").read[String] and
      (JsPath \ "documentHash").read[String] and
      (JsPath \ "assetType").read[String] and
      (JsPath \ "assetQuantity").read[String] and
      (JsPath \ "assetPrice").read[String] and
      (JsPath \ "quantityUnit").read[String] and
      (JsPath \ "ownerAddress").read[String] and
      (JsPath \ "locked").read[Boolean] and
      (JsPath \ "moderated").read[Boolean] and
      (JsPath \ "takerAddress").read[String]
    ) (Asset.apply _)

  implicit val assetWrites: OWrites[Asset] = Json.writes[Asset]

  case class Owner(ownerAddress: String, amount: String)

  implicit val ownerReads: Reads[Owner] = Json.reads[Owner]
  implicit val ownerWrites: OWrites[Owner] = Json.writes[Owner]

  case class Fiat(pegHash: String, transactionID: String, transactionAmount: MicroNumber, redeemedAmount: MicroNumber, owners: Option[Seq[Owner]])

  object Fiat {
    def apply(pegHash: String, transactionID: String, transactionAmount: String, redeemedAmount: String, owners: Option[Seq[Owner]]): Fiat = new Fiat(pegHash, transactionID, new MicroNumber(BigInt(transactionAmount)), new MicroNumber(BigInt(redeemedAmount)), owners)

    def unapply(arg: Fiat): Option[(String, String, String, String, Option[Seq[Owner]])] = Option(arg.pegHash, arg.transactionID, arg.transactionAmount.toMicroString, arg.redeemedAmount.toMicroString, arg.owners)
  }

  implicit val fiatReads: Reads[Fiat] = (
    (JsPath \ "pegHash").read[String] and
      (JsPath \ "transactionID").read[String] and
      (JsPath \ "transactionAmount").read[String] and
      (JsPath \ "redeemedAmount").read[String] and
      (JsPath \ "owners").readNullable[Seq[Owner]]
    ) (Fiat.apply _)

  implicit val fiatWrites: OWrites[Fiat] = Json.writes[Fiat]

  case class Value(address: String, coins: Option[Seq[Coin]], asset_peg_wallet: Option[Seq[Asset]], fiat_peg_wallet: Option[Seq[Fiat]], account_number: String, sequence: String)

  implicit val valueReads: Reads[Value] = Json.reads[Value]
  implicit val valueWrites: OWrites[Value] = Json.writes[Value]

  case class Response(value: Value) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val responseWrites: OWrites[Response] = Json.writes[Response]

}