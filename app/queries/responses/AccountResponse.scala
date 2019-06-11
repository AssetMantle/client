package queries.responses

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}

object AccountResponse {

  case class Coins(denom: String, amount: String)

  implicit val coinsReads: Reads[Coins] = Json.reads[Coins]

  implicit val assetReads: Reads[Asset] = (
    (JsPath \ "pegHash").read[String] and
      (JsPath \ "documentHash").read[String] and
      (JsPath \ "assetType").read[String] and
      (JsPath \ "assetQuantity").read[String] and
      (JsPath \ "assetPrice").read[String] and
      (JsPath \ "quantityUnit").read[String] and
      (JsPath \ "ownerAddress").read[String] and
      (JsPath \ "locked").read[Boolean] and
      (JsPath \ "moderated").read[Boolean]
    ) (Asset.apply _)

  case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, ownerAddress: String, locked: Boolean, unmoderated: Boolean)

  case class Owners(ownerAddress: String, amount: String)

  implicit val ownersReads: Reads[Owners] = Json.reads[Owners]

  implicit val fiatReads: Reads[Fiat] = Json.reads[Fiat]

  case class Fiat(pegHash: String, transactionID: String, transactionAmount: String, redeemedAmount: String, owners: Option[Seq[Owners]])

  case class Value(address: String, coins: Option[Seq[Coins]], assetPegWallet: Option[Seq[Asset]], fiatPegWallet: Option[Seq[Fiat]], account_number: String, sequence: String)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
