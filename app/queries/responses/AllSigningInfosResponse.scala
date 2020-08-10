package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.common.SigningInfo
import transactions.Abstract.BaseResponse

object AllSigningInfosResponse {

  case class Response(height: String, result: Seq[SigningInfo.Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
