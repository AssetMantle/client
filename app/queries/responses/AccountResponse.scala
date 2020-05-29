package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object AccountResponse {

  case class Coins(denom: String, amount: String)

  implicit val coinsReads: Reads[Coins] = Json.reads[Coins]

  implicit val assetReads: Reads[Asset] = Json.reads[Asset]

  case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, ownerAddress: String, locked: Boolean, moderated: Boolean, takerAddress: String)

  case class Owners(ownerAddress: String, amount: String)

  implicit val ownersReads: Reads[Owners] = Json.reads[Owners]

  implicit val fiatReads: Reads[Fiat] = Json.reads[Fiat]

  case class Fiat(pegHash: String, transactionID: String, transactionAmount: String, redeemedAmount: String, owners: Option[Seq[Owners]])

  case class Value(address: String, coins: Option[Seq[Coins]], asset_peg_wallet: Option[Seq[Asset]], fiat_peg_wallet: Option[Seq[Fiat]], account_number: String, sequence: String)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}