package queries.responses

import play.api.libs.ws.WSResponse
import transactions.Abstract.BaseResponse

object TruliooAuthenticationResponse {

  class Response(response: WSResponse) extends BaseResponse {
    val body: String = response.body.split(""""""")(1)
  }

}
