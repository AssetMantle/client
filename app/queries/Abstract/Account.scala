package queries.Abstract

import models.blockchain.{Account => blockchainAccount}
import models.common.Serializable.Coin

abstract class Account {
  val address: String
  val tokens: Seq[Coin]
  def toSerializableAccount(username: String): blockchainAccount
}
