package queries.responses.blockchain

import play.api.libs.json.{Json, OWrites, Reads}
import transactions.Abstract.BaseResponse

object MintingInflationResponse {

  case class Response(height: String, result: BigDecimal) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val responseWrites: OWrites[Response] = Json.writes[Response]
}
