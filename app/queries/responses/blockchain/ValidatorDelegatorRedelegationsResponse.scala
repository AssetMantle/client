package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Redelegation
import transactions.Abstract.BaseResponse

object ValidatorDelegatorRedelegationsResponse {

  case class Response(height: String, result: Seq[Redelegation.Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
