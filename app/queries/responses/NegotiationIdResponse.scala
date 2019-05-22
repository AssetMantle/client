package queries.responses

import play.api.libs.json.{Json, Reads}

object NegotiationIdResponse {

  case class Response(negotiationID: String)
  
  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
