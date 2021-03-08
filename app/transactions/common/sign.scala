package transactions.common

import models.common.Serializable.{Coin, Fee}
import queries.responses.common.Account.SinglePublicKey

object sign{

  case class Value(from_address:String,to_address:String, amount:Seq[Coin])
  case class Signature( signature:String, pub_key: SinglePublicKey)
  case class Message(`type`:String,value:Value)
  case class Tx(msg:Seq[Message], fee:Fee, memo:String="")

  case class StdSignMsg(account_number:String, chain_id:String, fee:Fee, memo:String, msgs:Seq[Message], sequence:String)

  case class StdTx(msg:Seq[Message], fee:Fee, signatures: Seq[Signature], memo:String="")

  case class SignMeta(account_number:String, chain_id:String, sequence:String)


}