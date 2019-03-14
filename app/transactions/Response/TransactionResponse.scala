package transactions.Response

import play.api.libs.json.{Json, Reads}

object TransactionResponse {

  implicit val keyValueReads: Reads[KeyValue] = Json.reads[KeyValue]

  implicit val responseReads: Reads[Response] = Json.reads[Response]

  case class KeyValue(Key: String, Value: String)

  case class Response(GasUsed: String, GasWanted: String, Height: String, Tags: Seq[KeyValue], TxHash: String, hello: String)

  implicit val kafkaResponseReads: Reads[KafkaResponse] = Json.reads[KafkaResponse]

  case class KafkaResponse(ticketID: String)

}
