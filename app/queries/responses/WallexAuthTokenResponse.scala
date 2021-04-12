package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object WallexAuthTokenResponse {

  case class Response(token: String) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
