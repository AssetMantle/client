package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Undelegation


object ValidatorDelegatorUndelegationResponse {

  case class Response(unbond: Undelegation.Result)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
