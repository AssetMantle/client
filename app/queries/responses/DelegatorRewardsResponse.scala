package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin
import transactions.Abstract.BaseResponse

object DelegatorRewardsResponse {

  case class Reward(validator_address: String, reward: Option[Seq[Coin]])

  implicit val rewardReads: Reads[Reward] = Json.reads[Reward]

  case class Result(rewards: Option[Seq[Reward]], total: Option[Seq[Coin]])

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(height: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
