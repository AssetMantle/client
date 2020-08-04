package queries.responses

import play.api.libs.json.{Json, OWrites, Reads}
import transactions.Abstract.BaseResponse

object NegotiationIdResponse {

  case class Response(negotiationID: String) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val responseWrites: OWrites[Response] = Json.writes[Response]
}
