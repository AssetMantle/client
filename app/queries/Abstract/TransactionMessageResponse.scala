package queries.Abstract

import models.Abstract.TransactionMessage

abstract class TransactionMessageResponse {
  def toTxMsg: TransactionMessage
}
