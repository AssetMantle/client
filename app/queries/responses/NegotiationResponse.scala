package queries.responses

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}
import transactions.Abstract.BaseResponse
import utilities.MicroNumber

object NegotiationResponse {

  case class Value(negotiationID: String, buyerAddress: String, sellerAddress: String, pegHash: String, bid: MicroNumber, time: String, buyerSignature: Option[String], sellerSignature: Option[String], buyerBlockHeight: Option[String], sellerBlockHeight: Option[String], buyerContractHash: Option[String], sellerContractHash: Option[String])

  object Value {
    def apply(negotiationID: String, buyerAddress: String, sellerAddress: String, pegHash: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String], buyerBlockHeight: Option[String], sellerBlockHeight: Option[String], buyerContractHash: Option[String], sellerContractHash: Option[String]): Value = new Value(negotiationID, buyerAddress, sellerAddress, pegHash, new MicroNumber(BigInt(bid)), time, buyerSignature, sellerSignature, buyerBlockHeight, sellerBlockHeight, buyerContractHash, sellerContractHash)
  }

  implicit val valueReads: Reads[Value] = (
    (JsPath \ "negotiationID").read[String] and
      (JsPath \ "buyerAddress").read[String] and
      (JsPath \ "sellerAddress").read[String] and
      (JsPath \ "pegHash").read[String] and
      (JsPath \ "bid").read[String] and
      (JsPath \ "time").read[String] and
      (JsPath \ "buyerSignature").readNullable[String] and
      (JsPath \ "sellerSignature").readNullable[String] and
      (JsPath \ "buyerBlockHeight").readNullable[String] and
      (JsPath \ "sellerBlockHeight").readNullable[String] and
      (JsPath \ "buyerContractHash").readNullable[String] and
      (JsPath \ "sellerContractHash").readNullable[String]
    ) (Value.apply _)

  case class Response(value: Value) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
