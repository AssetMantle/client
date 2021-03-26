package queries.responses.blockchain

import models.blockchain.Transaction
import models.common.Serializable
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Json, Reads}
import queries.Abstract.TransactionMessageResponse
import blockchain.messages.Messages.msgApply
import transactions.Abstract.BaseResponse
import blockchain.Abstract.{BlockchainStdMessage => BCTransaction}
import blockchain.common.Coin

object TransactionResponse {

  case class Log(msg_index: Int, log: String)

  implicit val logReads: Reads[Log] = Json.reads[Log]

  case class Fee(amount: Seq[Coin], gas: String) {
    def toFee: Serializable.Fee = Serializable.Fee(amount = amount.map(_.toCoin), gas = gas)
  }

  implicit val feeReads: Reads[Fee] = Json.reads[Fee]

  case class Msg(msgType: String, value: BCTransaction) {
    def toStdMsg: Serializable.StdMsg = Serializable.StdMsg(msgType, value.toTxMsg)
  }

  implicit val msgReads: Reads[Msg] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "value").read[JsObject]
    ) (msgApply _)

  case class Value(msg: Seq[Msg], fee: Fee)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Tx(value: Value)

  implicit val txReads: Reads[Tx] = Json.reads[Tx]

  case class Response(height: String, txhash: String, code: Option[Int], raw_log: String, logs: Option[Seq[Log]], gas_wanted: String, gas_used: String, tx: Tx, timestamp: String) extends BaseResponse {
    def toTransaction: Transaction = Transaction(hash = txhash, height = height.toInt, code = code, rawLog = raw_log, gasWanted = gas_wanted, gasUsed = gas_used,
      status = code.fold(true)(x => x == 0),
      messages = tx.value.msg.map(_.toStdMsg),
      fee = tx.value.fee.toFee,
      timestamp = timestamp)
  }

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
