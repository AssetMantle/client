package queries.responses

import models.blockchain
import play.api.libs.json.{Json, Reads}

object AccountResponse {

  case class Coins(denom: String, amount: String)

  implicit val coinsReads: Reads[Coins] = Json.reads[Coins]
  implicit val assetReads: Reads[Asset] = Json.reads[Asset]

  case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: String, assetPrice: String, quantityUnit: String, ownerAddress: String, locked: Boolean) {
    def applyToBlockchainAsset(ownerAddress: String): blockchain.Asset = blockchain.Asset(pegHash = pegHash, documentHash = documentHash, assetType = assetType, assetQuantity = assetQuantity.toInt, assetPrice = assetPrice.toInt, quantityUnit = quantityUnit, ownerAddress = ownerAddress, locked = locked)
  }


  case class Fiat(pegHash: String, transactionID: String, transactionAmount: String, redeemedAmount: String)

  implicit val fiatReads: Reads[Fiat] = Json.reads[Fiat]

  case class Value(address: String, coins: Option[Seq[Coins]], assetPegWallet: Option[Seq[Asset]], fiatPegWallet: Option[Seq[Fiat]], account_number: String, sequence: String)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
