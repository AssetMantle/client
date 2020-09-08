package queries.responses

import play.api.libs.ws.WSResponse
import transactions.Abstract.BaseResponse

object MnemonicResponse {

  class Response(response: WSResponse) extends BaseResponse {
    val body: String = response.body
  }

}
