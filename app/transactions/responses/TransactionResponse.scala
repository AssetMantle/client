package transactions.responses

import play.api.libs.json.{Json, Reads}

object TransactionResponse {

  case class BlockResponse(height: String, txhash: String, gas_wanted: String, gas_used: String, code: Option[Int])

  implicit val blockResponseReads: Reads[BlockResponse] = Json.reads[BlockResponse]

  case class SyncResponse(height: String, txhash: String)

  implicit val syncResponseReads: Reads[SyncResponse] = Json.reads[SyncResponse]

  case class AsyncResponse(height: String, txhash: String)

  implicit val asyncResponseReads: Reads[AsyncResponse] = Json.reads[AsyncResponse]

  case class BlockModeErrorResponse(error: String)

  implicit val blockModeErrorResponseReads: Reads[BlockModeErrorResponse] = Json.reads[BlockModeErrorResponse]

  case class KafkaResponse(ticketID: String)

  implicit val kafkaResponseReads: Reads[KafkaResponse] = Json.reads[KafkaResponse]

}
