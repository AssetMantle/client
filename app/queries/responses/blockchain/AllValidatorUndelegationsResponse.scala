package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Undelegation


object AllValidatorUndelegationsResponse {

  case class Response(unbonding_responses: Seq[Undelegation.Result])

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
