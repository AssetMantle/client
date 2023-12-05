package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin


object CommunityPoolResponse {

  case class Response(pool: Seq[Coin])

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
