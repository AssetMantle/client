package transactions.Response

import play.api.libs.json.{Json, Reads}

object NegotiationResponse {

  case class Value(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String])

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
