package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Validator
import transactions.Abstract.BaseResponse

object ValidatorsResponse {

  case class Response(validators: Seq[Validator.Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
