package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin


object TotalSupplyResponse {

  case class Response(supply: Seq[Coin])

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
