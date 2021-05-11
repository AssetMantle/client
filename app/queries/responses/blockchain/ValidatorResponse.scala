package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Validator
import transactions.Abstract.BaseResponse

object ValidatorResponse {

  case class Response(validator: Validator.Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
