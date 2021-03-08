package transactions.common

import models.common.Serializable._
import play.api.libs.json.{Json, OWrites}
import queries.responses.common.Account.SinglePublicKey

object sign{

  case class Value(from_address:String,to_address:String, amount:Seq[Coin])
  implicit val valueWrites: OWrites[Value] = Json.writes[Value]

  case class Signature2( signature:String, pub_key: SinglePublicKey)
  case class Message(`type`:String,value:Value)
  implicit val messageWrites: OWrites[Message] = Json.writes[Message]

  case class Tx(msg:Seq[Message], fee:Fee, memo:String="")


  case class StdSignMsg(account_number:String, chain_id:String, fee:Fee, memo:String, msgs:Seq[Message], sequence:String)
  implicit val stdSignMsgWrites: OWrites[StdSignMsg] = Json.writes[StdSignMsg]

  case class StdTx(msg:Seq[Message], fee:Fee, signatures: Seq[Signature2], memo:String="")

  case class SignMeta(account_number:String, chain_id:String, sequence:String)


}