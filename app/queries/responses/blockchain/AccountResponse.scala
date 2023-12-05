package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.Abstract.Account

object AccountResponse {
  case class Response(account: Account)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
