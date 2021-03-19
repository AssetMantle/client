package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Delegation
import transactions.Abstract.BaseResponse

object AllValidatorDelegationsResponse {

  case class Response(delegation_responses: Seq[Delegation.Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
