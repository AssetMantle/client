package transactions.responses

import play.api.libs.json.{Json, OWrites, Reads}
import transactions.Abstract.BaseResponse

object TransactionResponse {

  case class BlockResponse(height: String, txhash: String, gas_wanted: String, gas_used: String, code: Option[Int]) extends BaseResponse

  implicit val blockResponseReads: Reads[BlockResponse] = Json.reads[BlockResponse]
  implicit val blockResponseWrites: OWrites[BlockResponse] = Json.writes[BlockResponse]

  case class SyncResponse(height: String, txhash: String) extends BaseResponse

  implicit val syncResponseReads: Reads[SyncResponse] = Json.reads[SyncResponse]
  implicit val syncResponseWrites: OWrites[SyncResponse] = Json.writes[SyncResponse]

  case class AsyncResponse(height: String, txhash: String) extends BaseResponse

  implicit val asyncResponseReads: Reads[AsyncResponse] = Json.reads[AsyncResponse]
  implicit val asyncResponseWrites: OWrites[AsyncResponse] = Json.writes[AsyncResponse]

  case class ErrorResponse(error: String) extends BaseResponse

  implicit val ErrorResponseReads: Reads[ErrorResponse] = Json.reads[ErrorResponse]
  implicit val ErrorResponseWrites: OWrites[ErrorResponse] = Json.writes[ErrorResponse]

  case class KafkaResponse(ticketID: String) extends BaseResponse

  implicit val kafkaResponseReads: Reads[KafkaResponse] = Json.reads[KafkaResponse]
  implicit val kafkaResponseWrites: OWrites[KafkaResponse] = Json.writes[KafkaResponse]
}
