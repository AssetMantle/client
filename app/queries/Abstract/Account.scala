package queries.Abstract

import models.blockchain.{Account => blockchainAccount}

abstract class Account {
  val address: String
  def toSerializableAccount(username: String): blockchainAccount
}
