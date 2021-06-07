package queries.responses

import play.api.libs.json.{JsValue, Json, Reads}
import transactions.Abstract.BaseResponse

object DocusignRegenerateTokenResponse {

  case class Response(access_token: String, token_type: String, expires_in: Long, refresh_token: String) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}