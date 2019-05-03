package queries.responses

import play.api.libs.json.{Json, Reads}

object TransactionHashResponse {

  case class Fee(gas: String)

  implicit val feeReads: Reads[Fee] = Json.reads[Fee]

  case class Value(fee: Fee)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Tx(value: Value)

  implicit val txReads: Reads[Tx] = Json.reads[Tx]

  case class Response(hash: String, height: String, tx: Tx)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
