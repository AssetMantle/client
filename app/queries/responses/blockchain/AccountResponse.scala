package queries.responses.blockchain

import blockchain.common.Account
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object AccountResponse {

  case class Response(height: String, result: Account.Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
