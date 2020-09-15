package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Validator
import transactions.Abstract.BaseResponse

object ValidatorsResponse {

  case class Response(height: String, result: Seq[Validator.Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
