package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse
import queries.Abstract.Account

object AccountResponse {

  import queries.responses.common.Accounts.accountReads

  case class Response(result: Account) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
