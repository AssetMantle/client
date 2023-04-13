package queries.responses.blockchain

import models.blockchain.Transaction
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object TransactionByHashResponse {

  case class TxResult(code: Int, log: String, gas_wanted: String, gas_used: String)

  implicit val txResultReads: Reads[TxResult] = Json.reads[TxResult]

  case class Result(hash: String, height: String, tx_result: TxResult, tx: String) {
    def success: Boolean = this.tx_result.code == 0

    def toTransactionWithLog: Transaction = Transaction(hash = this.hash, height = this.height.toInt, code = this.tx_result.code, gasWanted = this.tx_result.gas_wanted, gasUsed = this.tx_result.gas_used, txBytes = utilities.Secrets.base64Decoder(this.tx), log = Option(this.tx_result.log))
  }

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
