package queries.responses

import models.blockchain
import play.api.libs.json.{Json, Reads}

object AccountResponse {

  case class Coins(denom: String, amount: String)

  implicit val coinsReads: Reads[Coins] = Json.reads[Coins]

  implicit val assetReads: Reads[blockchain.Asset] = Json.reads[blockchain.Asset]

  implicit val fiatReads: Reads[blockchain.Fiat] = Json.reads[blockchain.Fiat]

  case class Value(address: String, coins: Option[Seq[Coins]], assetPegWallet: Option[Seq[blockchain.Asset]], fiatPegWallet: Option[Seq[blockchain.Fiat]], account_number: String, sequence: String)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
