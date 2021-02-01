package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Undelegation
import transactions.Abstract.BaseResponse

object ValidatorDelegatorUndelegationsResponse {

  case class Response(height: String, result: Undelegation.Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
