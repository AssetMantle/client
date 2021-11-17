package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin
import transactions.Abstract.BaseResponse

object ValidatorRewardsResponse {

  case class Result(operator_address: String, self_bond_rewards: Seq[Coin], val_commission: Seq[Coin])

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
