package queries.responses

import play.api.libs.json.{Json, Reads}
import play.api.libs.ws.WSResponse
import transactions.Abstract.BaseResponse

object TruliooConsentsResponse {

  class Response(response: WSResponse) extends BaseResponse {
    val body: Seq[String] = response.body.replaceAll(""""""", "")
      .replaceAll("[\\[\\]]","")
      .split(",").map(_.trim)
  }
}
