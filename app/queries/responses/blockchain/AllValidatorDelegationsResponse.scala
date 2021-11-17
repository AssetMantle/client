package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Delegation
import transactions.Abstract.BaseResponse

object AllValidatorDelegationsResponse {

  case class Response(result: Seq[Delegation]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
