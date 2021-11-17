package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.SigningInfo
import transactions.Abstract.BaseResponse

object SigningInfoResponse {

  case class Response(result: SigningInfo.Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
