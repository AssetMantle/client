package queries.`abstract`

import models.common.Serializable.StdMsg

abstract class TransactionMessageResponse {
  def toStdMsg: StdMsg
}
