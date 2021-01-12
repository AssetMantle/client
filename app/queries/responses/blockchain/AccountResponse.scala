package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Account
import transactions.Abstract.BaseResponse

object AccountResponse {

  case class Response(height: String, result: Account.Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
