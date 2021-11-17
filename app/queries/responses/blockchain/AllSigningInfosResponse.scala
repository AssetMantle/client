package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.SigningInfo
import transactions.Abstract.BaseResponse

object AllSigningInfosResponse {

  case class Response(result: Seq[SigningInfo.Result]) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
