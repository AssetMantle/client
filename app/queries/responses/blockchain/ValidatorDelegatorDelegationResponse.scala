package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Delegation


object ValidatorDelegatorDelegationResponse {

  case class Response(delegation_response: Delegation.Result)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
