package queries.responses.blockchain

import models.blockchain.Transaction
import models.common.Serializable
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Json, Reads}
import queries.Abstract.{PublicKey, TransactionMessageResponse}
import queries.responses.common.Coin
import queries.responses.common.PublicKey._
import queries.responses.common.TransactionMessageResponses.msgApply
import transactions.Abstract.BaseResponse

object TransactionResponse {

  case class Fee(amount: Seq[Coin], gas_limit: String, payer: String, granter: String) {
    def toFee: Serializable.Fee = Serializable.Fee(amount = amount.map(_.toCoin), gasLimit = gas_limit, payer = payer, granter = granter)
  }

  implicit val feeReads: Reads[Fee] = Json.reads[Fee]

  case class SignerInfo(public_key: PublicKey, sequence: String)

  implicit val signerInfoReads: Reads[SignerInfo] = Json.reads[SignerInfo]

  case class AuthInfo(fee: Fee, signer_infos: Seq[SignerInfo])

  implicit val authInfoReads: Reads[AuthInfo] = Json.reads[AuthInfo]

  case class Msg(msgType: String, value: TransactionMessageResponse) {
    def toStdMsg: Serializable.StdMsg = Serializable.StdMsg(msgType, value.toTxMsg)
  }

  implicit val msgReads: Reads[Msg] = (
    (JsPath \ "@type").read[String] and
      JsPath.read[JsObject]
    ) (msgApply _)

  case class TxBody(messages: Seq[Msg], memo: String, timeout_height: String)

  implicit val txBodyReads: Reads[TxBody] = Json.reads[TxBody]

  case class Tx(body: TxBody, auth_info: AuthInfo, signatures: Seq[String])

  implicit val txReads: Reads[Tx] = Json.reads[Tx]

  case class TxResponse(height: String, txhash: String, code: Int, raw_log: String, gas_wanted: String, gas_used: String, tx: Tx, timestamp: String) {
    def toTransaction: Transaction = Transaction(
      hash = txhash,
      height = height.toInt,
      code = code,
      rawLog = raw_log,
      gasWanted = gas_wanted,
      gasUsed = gas_used,
      status = code == 0,
      messages = tx.body.messages.map(_.toStdMsg),
      fee = tx.auth_info.fee.toFee,
      memo = tx.body.memo,
      timestamp = timestamp)
  }

  implicit val txResponseReads: Reads[TxResponse] = Json.reads[TxResponse]

  case class Response(txResponse: TxResponse) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
