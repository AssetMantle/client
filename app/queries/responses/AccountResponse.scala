package queries.responses

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}
import transactions.Abstract.BaseResponse
import utilities.MicroLong

object AccountResponse {

  case class Coin(denom: String, amount: MicroLong)

  object Coin {
    def apply(denom: String, amount: String): Coin = new Coin(denom, new MicroLong(amount.toLong))
  }

  implicit val coinReads: Reads[Coin] = (
    (JsPath \ "denom").read[String] and
      (JsPath \ "amount").read[String]
    ) (Coin.apply _)

  case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: MicroLong, assetPrice: MicroLong, quantityUnit: String, ownerAddress: String, locked: Boolean, moderated: Boolean, takerAddress: String)

  object Asset {
    def apply(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, ownerAddress: String, locked: Boolean, moderated: Boolean, takerAddress: String): Asset = new Asset(pegHash, documentHash, assetType, new MicroLong(assetQuantity.toLong), new MicroLong(assetPrice.toLong), quantityUnit, ownerAddress, locked, moderated, takerAddress)
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

  case class Owner(ownerAddress: String, amount: String)

  case class Fiat(pegHash: String, transactionID: String, transactionAmount: MicroLong, redeemedAmount: MicroLong, owners: Option[Seq[Owner]])

  object Fiat {
    def apply(pegHash: String, transactionID: String, transactionAmount: String, redeemedAmount: String, owners: Option[Seq[Owner]]): Fiat = new Fiat(pegHash, transactionID, new MicroLong(transactionAmount.toLong), new MicroLong(redeemedAmount.toLong), owners)
  }

  implicit val ownerReads: Reads[Owner] = Json.reads[Owner]

  implicit val fiatReads: Reads[Fiat] = (
    (JsPath \ "pegHash").read[String] and
      (JsPath \ "transactionID").read[String] and
      (JsPath \ "transactionAmount").read[String] and
      (JsPath \ "redeemedAmount").read[String] and
      (JsPath \ "owners").readNullable[Seq[Owner]]
    ) (Fiat.apply _)

  case class Value(address: String, coins: Option[Seq[Coin]], asset_peg_wallet: Option[Seq[Asset]], fiat_peg_wallet: Option[Seq[Fiat]], account_number: String, sequence: String)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}