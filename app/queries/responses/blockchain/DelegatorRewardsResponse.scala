package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin
import transactions.Abstract.BaseResponse

object DelegatorRewardsResponse {

  case class Reward(validator_address: String, reward: Seq[Coin])

  implicit val rewardReads: Reads[Reward] = Json.reads[Reward]

  case class Response(rewards: Seq[Reward], total: Seq[Coin]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
