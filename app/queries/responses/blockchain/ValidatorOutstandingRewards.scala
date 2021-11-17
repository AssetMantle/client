package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin
import transactions.Abstract.BaseResponse

object ValidatorOutstandingRewards {

  case class Rewards(rewards: Seq[Coin])

  implicit val rewardsReads: Reads[Rewards] = Json.reads[Rewards]

  case class Response(result: Seq[Coin]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
