package queries.responses

import play.api.libs.ws.WSResponse

object ZoneResponse {

  class Response(response: WSResponse) {
    val body: String = response.body.split(""""""")(1)
  }
}
