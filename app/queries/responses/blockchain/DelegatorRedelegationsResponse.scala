package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Redelegation


object DelegatorRedelegationsResponse {

  case class Response(redelegation_responses: Seq[Redelegation.Result])

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
