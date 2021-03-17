package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.Abstract.Account
import transactions.Abstract.BaseResponse
import queries.responses.common.Account._

object AccountResponse {

  case class Response(account: Account) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
