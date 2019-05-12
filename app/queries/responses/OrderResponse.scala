package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.AccountResponse.{Asset, Fiat}

object OrderResponse {

  implicit val assetReads: Reads[Asset] = Json.reads[Asset]

  implicit val fiatReads: Reads[Fiat] = Json.reads[Fiat]

  case class Value(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], fiatPegWallet: Option[Seq[Fiat]], assetPegWallet: Option[Seq[Asset]])

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
