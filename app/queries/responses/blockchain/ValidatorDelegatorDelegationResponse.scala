package queries.responses.blockchain

import blockchainTx.common.Delegation
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object ValidatorDelegatorDelegationResponse {

  case class Response(height: String, result: Delegation.Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
