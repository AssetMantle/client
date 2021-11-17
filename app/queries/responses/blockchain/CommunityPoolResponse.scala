package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin
import transactions.Abstract.BaseResponse

object CommunityPoolResponse {

  case class Response(result: Seq[Coin]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
