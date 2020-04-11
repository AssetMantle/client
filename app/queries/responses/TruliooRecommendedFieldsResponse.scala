package queries.responses

import play.api.libs.json.{JsValue, Json, Reads}
import transactions.Abstract.BaseResponse

object TruliooRecommendedFieldsResponse {

  case class Response(title: String, `type`: String, properties: JsValue) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
