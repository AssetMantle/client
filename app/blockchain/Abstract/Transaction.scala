package blockchain.Abstract

import models.Abstract.TransactionMessage

abstract class Transaction {
  def toTxMsg: TransactionMessage
}
