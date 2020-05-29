package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.AccountResponse.{Asset, Fiat}
import transactions.Abstract.BaseResponse


object OrderResponse {

  case class Value(negotiation_id: String, fiat_proof_hash: String, awb_proof_hash: String, fiat_peg_wallet: Option[Seq[Fiat]], asset_peg_wallet: Option[Seq[Asset]])

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
