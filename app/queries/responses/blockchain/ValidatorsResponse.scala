package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Validator


object ValidatorsResponse {

  case class Response(validators: Seq[Validator.Result])

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
