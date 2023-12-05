package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Delegation


object AllValidatorDelegationsResponse {

  case class Response(delegation_responses: Seq[Delegation.Result])

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
