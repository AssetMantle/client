package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object MintingInflationResponse {

  case class Response(result: String) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
