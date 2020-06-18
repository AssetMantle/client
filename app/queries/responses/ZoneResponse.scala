package queries.responses

import play.api.libs.ws.WSResponse
import transactions.Abstract.BaseResponse

object ZoneResponse {

  class Response(response: WSResponse) extends BaseResponse {
    println(response.body)
    println(response.body.split(""""""")(1))
    val body: String = response.body.split(""""""")(1)
  }

}
