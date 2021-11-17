package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Undelegation
import transactions.Abstract.BaseResponse

object ValidatorDelegatorUndelegationResponse {

  case class Response(result: Undelegation.Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
