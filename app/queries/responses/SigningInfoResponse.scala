package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.common.SigningInfo
import transactions.Abstract.BaseResponse

object SigningInfoResponse {

  case class Response(height: String, result: SigningInfo.Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
