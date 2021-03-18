package queries.Abstract

import blockchain.common.Coin

abstract class Account {
  val address: String
  val coins: Seq[Coin]
  val publicKeyValue: String
  val accountNumber: String
  val sequence: String
}
