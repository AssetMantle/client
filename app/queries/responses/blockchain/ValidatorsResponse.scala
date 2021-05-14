package queries.responses.blockchain

import blockchainTx.common.Validator
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object ValidatorsResponse {

  case class Response(height: String, result: Seq[Validator.Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
