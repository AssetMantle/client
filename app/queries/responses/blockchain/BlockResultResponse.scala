package queries.responses.blockchain

import blockchainTx.common.Event
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object BlockResultResponse {

  case class Result(height: String, begin_block_events: Seq[Event], end_block_events: Option[Seq[Event]])

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
