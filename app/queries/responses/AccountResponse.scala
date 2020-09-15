package queries.responses

import play.api.libs.json._
import queries.responses.common.Account
import transactions.Abstract.BaseResponse

object AccountResponse {

  case class Response(height: String, result: Account.Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}