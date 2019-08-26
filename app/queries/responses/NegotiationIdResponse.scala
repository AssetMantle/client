package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.responses.ResponseEntity

object NegotiationIdResponse {

  case class Response(negotiationID: String)  extends ResponseEntity

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
