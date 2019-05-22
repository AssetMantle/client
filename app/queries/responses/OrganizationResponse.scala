package queries.responses

import play.api.libs.json.{Json, Reads}

object OrganizationResponse {

  case class Response(address: String, zoneID: String)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
