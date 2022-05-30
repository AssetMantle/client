package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object TransactionByHeightResponse {

  case class TxResult(code: Int)

  implicit val txResultReads: Reads[TxResult] = Json.reads[TxResult]

  case class Tx(hash: String, height: String, index: Int, tx_result: TxResult) {
    def success: Boolean = this.tx_result.code == 0
  }

  implicit val txReads: Reads[Tx] = Json.reads[Tx]

  case class Result(txs: Seq[Tx], total_count: String)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
