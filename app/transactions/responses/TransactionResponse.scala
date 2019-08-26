package transactions.responses

import play.api.libs.json.{Json, Reads}

abstract class ResponseEntity {

}

object TransactionResponse {

  case class BlockResponse(height: String, txhash: String, gas_wanted: String, gas_used: String, code: Option[Int]) extends ResponseEntity

  implicit val blockResponseReads: Reads[BlockResponse] = Json.reads[BlockResponse]

  case class SyncResponse(height: String, txhash: String) extends ResponseEntity

  implicit val syncResponseReads: Reads[SyncResponse] = Json.reads[SyncResponse]

  case class AsyncResponse(height: String, txhash: String) extends ResponseEntity

  implicit val asyncResponseReads: Reads[AsyncResponse] = Json.reads[AsyncResponse]

  case class BlockModeErrorResponse(error: String) extends ResponseEntity

  implicit val blockModeErrorResponseReads: Reads[BlockModeErrorResponse] = Json.reads[BlockModeErrorResponse]

  case class KafkaResponse(ticketID: String) extends ResponseEntity

  implicit val kafkaResponseReads: Reads[KafkaResponse] = Json.reads[KafkaResponse]

}
