package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Validator


object ValidatorResponse {

  case class Response(validator: Validator.Result)

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
