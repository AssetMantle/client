package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Undelegation
import transactions.Abstract.BaseResponse

object AllValidatorUndelegationsResponse {

  case class Response(height: String, result: Seq[Undelegation.Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
