package queries.responses.blockchain

import blockchain.common.Coin
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object ValidatorOutstandingRewards {

  case class Response(height: String, result: Option[Seq[Coin]]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
