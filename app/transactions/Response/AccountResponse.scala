package transactions.Response

import play.api.libs.json.{Json, Reads}

object Account {

  case class Coins(denom: String, amount: String)

  implicit val coinsReads: Reads[Coins] = Json.reads[Coins]

  case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, ownerAddress: String, locked: Boolean)

  implicit val assetReads: Reads[Asset] = Json.reads[Asset]

  case class Fiat(pegHash: String, transactionID: String, transactionAmount: String, redeemedAmount: String)

  implicit val fiatReads: Reads[Fiat] = Json.reads[Fiat]

  case class Value(address: String, coins: Option[Seq[Coins]], assetPegWallet: Option[Seq[Asset]], fiatPegWallet: Option[Seq[Fiat]], account_number: String, sequence: String)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
