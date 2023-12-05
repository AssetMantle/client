package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}


object MintingInflationResponse {

  case class Response(inflation: String)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
