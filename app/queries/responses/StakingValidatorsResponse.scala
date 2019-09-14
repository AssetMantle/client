package queries.responses

import play.api.libs.json.{Json, OWrites, Reads}
import transactions.Abstract.BaseResponse

object StakingValidatorsResponse {

  case class Result(operator_address: String, consensus_pubkey: String, jailed: Boolean, status: Int, tokens: String, delegator_shares: String) extends BaseResponse

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  implicit val resultWrites: OWrites[Result] = Json.writes[Result]

  case class Response(result: Seq[Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

  implicit val responseWrites: OWrites[Response] = Json.writes[Response]
}
