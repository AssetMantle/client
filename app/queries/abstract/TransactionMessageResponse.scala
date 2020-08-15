package queries.`abstract`

import models.`abstract`.TransactionMessage

abstract class TransactionMessageResponse {
  def toTxMsg: TransactionMessage
}
