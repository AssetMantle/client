package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object AccountResponse {

  case class Result(address: Option[String], public_key: Option[String], account_number: Option[String], sequence: Option[String])

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(height: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}