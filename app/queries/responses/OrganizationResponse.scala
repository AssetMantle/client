package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object OrganizationResponse {

  case class Response(address: String, zoneID: String) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
