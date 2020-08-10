package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object ValidatorsResponse {

  case class Response(height: String, result: Seq[ValidatorResponse.Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
