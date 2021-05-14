package queries.responses.blockchain

import blockchainTx.common.SigningInfo
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object SigningInfoResponse {

  case class Response(height: String, result: SigningInfo.Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
