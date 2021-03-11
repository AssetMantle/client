package transactions.common

import models.common.Serializable._
import play.api.libs.json.{Json, OWrites}
import queries.responses.common.Account.SinglePublicKey
import queries.responses.common.Account.singlePublicKeyWrites

object sign{

  case class Value(amount:Seq[Coin], from_address:String, to_address:String )
  implicit val valueWrites: OWrites[Value] = Json.writes[Value]

  case class Signature( signature:String, pub_key: SinglePublicKey)
  implicit val signatureWrites: OWrites[Signature] = Json.writes[Signature]

  case class SendCoinMessage(`type`:String, value:Value)
  implicit val messageWrites: OWrites[SendCoinMessage] = Json.writes[SendCoinMessage]

  case class Tx(msg:Seq[SendCoinMessage], fee:Fee, memo:String="")


  case class StdSignMsg(account_number:String, chain_id:String, fee:Fee, memo:String, msgs:Seq[SendCoinMessage], sequence:String)
  implicit val stdSignMsgWrites: OWrites[StdSignMsg] = Json.writes[StdSignMsg]

  case class StdTx(msg:Seq[SendCoinMessage], fee:Fee, signatures: Seq[Signature], memo:String="")

  case class SignMeta(account_number:String, chain_id:String, sequence:String)


}