package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object NegotiationResponse {

  case class Value(negotiationID: String, buyerAddress: String, sellerAddress: String, pegHash: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String], buyerBlockHeight: Option[String], sellerBlockHeight: Option[String], buyerContractHash: Option[String], sellerContractHash: Option[String])

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
