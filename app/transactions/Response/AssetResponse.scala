package transactions.Response

import play.api.libs.json.{Json, Reads}

object AssetResponse {

  case class Value(pegHash: String, ownerAddress: String, locked: Boolean)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
