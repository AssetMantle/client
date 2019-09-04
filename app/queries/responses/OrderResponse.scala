package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.AccountResponse.{Asset, Fiat}
import transactions.Abstract.BaseResponse


object OrderResponse {

  case class Value(negotiationID: String, fiatProofHash: String, awbProofHash: String, fiatPegWallet: Option[Seq[Fiat]], assetPegWallet: Option[Seq[Asset]])

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}