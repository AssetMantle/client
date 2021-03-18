package transactions.request

import blockchain.Abstract.Transaction
import models.common.Serializable.{Coin, Fee}
import play.api.Logger
import play.api.libs.json.{Json, OWrites}
import blockchain.common.Account.SinglePublicKey
import blockchain.messages.Messages.transactionWrites

object Serializable{

  private implicit val module: String = constants.Module.TRANSACTIONS_REQUEST_SERIALIZABLE

  private implicit val logger: Logger = Logger(this.getClass)

  case class Signature(signature: String, pub_key: SinglePublicKey)

  implicit val signatureWrites: OWrites[Signature] = Json.writes[Signature]

  case class Message(`type`: String, value: Transaction)

  implicit val messageWrites: OWrites[Message] = Json.writes[Message]

  case class Tx(msg: Seq[Message], fee: Fee, memo: String = "")

  case class StdSignMsg(account_number: String, chain_id: String, fee: Fee, memo: String, msgs: Seq[Message], sequence: String)

  implicit val stdSignMsgWrites: OWrites[StdSignMsg] = Json.writes[StdSignMsg]

  case class StdTx(msg: Seq[Message], fee: Fee, signatures: Seq[Signature], memo: String = "")

  implicit val stdTxWrites: OWrites[StdTx] = Json.writes[StdTx]

  case class SignMeta(account_number: String, chain_id: String, sequence: String)

}