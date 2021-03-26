package queries.responses.blockchain

import blockchain.common.SigningInfo
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object AllSigningInfosResponse {

  case class Response(height: String, result: Seq[SigningInfo.Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
