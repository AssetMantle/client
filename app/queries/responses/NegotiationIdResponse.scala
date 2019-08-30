package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object NegotiationIdResponse {

  case class Response(negotiationID: String) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
