package queries.responses.blockchain

import models.blockchain.Transaction
import models.common.Serializable
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Json, Reads}
import queries.Abstract.{PublicKey, TransactionMessageResponse}
import queries.responses.common.Coin
import queries.responses.common.PublicKeys._
import queries.responses.common.TransactionMessageResponses.msgApply
import transactions.Abstract.BaseResponse

object TransactionResponse {

  case class Fee(amount: Seq[Coin], gas: String) {
    def toFee: Serializable.Fee = Serializable.Fee(amount = amount.map(_.toCoin), gas = gas.toInt)
  }

  implicit val feeReads: Reads[Fee] = Json.reads[Fee]

  case class Signature(pub_key: PublicKey, signature: String)

  implicit val signatureInfoReads: Reads[Signature] = Json.reads[Signature]

  case class Msg(msgType: String, value: TransactionMessageResponse) {
    def toStdMsg: Serializable.StdMsg = Serializable.StdMsg(msgType, value.toTxMsg)
  }

  implicit val msgReads: Reads[Msg] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "value").read[JsObject]
    ) (msgApply _)

  case class TxBody(msg: Seq[Msg], fee: Fee, signatures: Seq[Signature], memo: String)

  implicit val txBodyReads: Reads[TxBody] = Json.reads[TxBody]

  case class Tx(value: TxBody)

  implicit val txReads: Reads[Tx] = Json.reads[Tx]

  case class Response(height: String, txhash: String, code: Option[Int], raw_log: String, gas_wanted: String, gas_used: String, tx: Tx, timestamp: String) extends BaseResponse {
    def toTransaction: Transaction = Transaction(
      hash = txhash,
      height = height.toInt,
      code = code.getOrElse(0),
      rawLog = raw_log,
      gasWanted = gas_wanted,
      gasUsed = gas_used,
      status = code.getOrElse(0) == 0,
      messages = tx.value.msg.map(_.toStdMsg),
      fee = tx.value.fee.toFee,
      memo = tx.value.memo,
      timestamp = timestamp
    )
  }

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
