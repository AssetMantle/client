package queries.responses.blockchain

import blockchainTx.common.Redelegation
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object ValidatorDelegatorRedelegationsResponse {

  case class Response(height: String, result: Seq[Redelegation.Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
