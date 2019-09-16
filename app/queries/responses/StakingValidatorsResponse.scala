package queries.responses

import play.api.libs.json.{Json, OWrites, Reads}
import transactions.Abstract.BaseResponse

object StakingValidatorsResponse {

  case class Response(operator_address: String, consensus_pubkey: String, jailed: Boolean, status: Int, tokens: String, delegator_shares: String) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

  implicit val seqResponseReads: Reads[Seq[Response]] = Reads.seq[Response]

  implicit val responseWrites: OWrites[Response] = Json.writes[Response]

}
