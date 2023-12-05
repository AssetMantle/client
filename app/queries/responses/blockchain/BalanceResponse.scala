package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin


object BalanceResponse {

  case class Response(balances: Seq[Coin])

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
