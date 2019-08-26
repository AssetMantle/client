package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.responses.ResponseEntity

object OrganizationResponse {

  case class Response(address: String, zoneID: String)  extends ResponseEntity

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
