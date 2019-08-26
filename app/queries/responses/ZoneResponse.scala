package queries.responses

import play.api.libs.ws.WSResponse
import transactions.responses.ResponseEntity

object ZoneResponse {

  class Response(response: WSResponse) extends ResponseEntity{
    val body: String = response.body.split(""""""")(1)
  }
}
