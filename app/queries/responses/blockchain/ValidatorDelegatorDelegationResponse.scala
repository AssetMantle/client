package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Delegation
import transactions.Abstract.BaseResponse

object ValidatorDelegatorDelegationResponse {

  case class Response(delegation_response: Delegation.Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
