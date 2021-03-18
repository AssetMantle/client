package queries.responses.blockchain

import blockchain.common.Undelegation
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object ValidatorDelegatorUndelegationsResponse {

  case class Response(height: String, result: Undelegation.Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
