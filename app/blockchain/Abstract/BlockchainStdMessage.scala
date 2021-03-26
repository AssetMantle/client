package blockchain.Abstract

import models.Abstract.TransactionMessage

abstract class BlockchainStdMessage {
  def toTxMsg: TransactionMessage
}
