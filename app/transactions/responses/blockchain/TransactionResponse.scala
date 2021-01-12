package transactions.responses.blockchain

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object TransactionResponse {

  case class BlockResponse(height: String, txhash: String, gas_wanted: String, gas_used: String, code: Option[Int]) extends BaseResponse

  implicit val blockResponseReads: Reads[BlockResponse] = Json.reads[BlockResponse]

  case class SyncResponse(height: String, txhash: String) extends BaseResponse

  implicit val syncResponseReads: Reads[SyncResponse] = Json.reads[SyncResponse]

  case class AsyncResponse(height: String, txhash: String) extends BaseResponse

  implicit val asyncResponseReads: Reads[AsyncResponse] = Json.reads[AsyncResponse]

  case class ErrorResponse(error: String) extends BaseResponse

  implicit val ErrorResponseReads: Reads[ErrorResponse] = Json.reads[ErrorResponse]

  case class KafkaResponse(ticketID: String) extends BaseResponse

  implicit val kafkaResponseReads: Reads[KafkaResponse] = Json.reads[KafkaResponse]

}
