package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin
import transactions.Abstract.BaseResponse

object ValidatorDistributionResponse {

  //TODO Raise an issue for the following
  //Bug in cosmos-sdk, the actual value send in `operator_address` is validator's account address instead of its operator address
  case class Result(operator_address: String, self_bond_rewards: Option[Seq[Coin]], val_commission: Option[Seq[Coin]])

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(height: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
