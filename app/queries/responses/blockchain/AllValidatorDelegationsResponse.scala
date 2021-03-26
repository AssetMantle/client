package queries.responses.blockchain

import blockchain.common.Delegation
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object AllValidatorDelegationsResponse {

  case class Response(height: String, result: Seq[Delegation.Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
