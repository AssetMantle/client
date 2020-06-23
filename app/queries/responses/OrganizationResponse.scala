package queries.responses

import play.api.libs.json.{Json, OWrites, Reads}
import queries.responses.BlockDetailsResponse.BlockID
import transactions.Abstract.BaseResponse

object OrganizationResponse {

  case class Response(address: String, zoneID: String) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val responseWrites: OWrites[Response] = Json.writes[Response]
}
