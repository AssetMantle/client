package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueFiatRequest {
  val form = Form(
    mapping(
      constants.Form.TRANSACTION_ID -> nonEmptyText(minLength = constants.FormConstraint.TRANSACTION_ID_MINIMUM_LENGTH, maxLength = constants.FormConstraint.TRANSACTION_ID_MAXIMUM_LENGTH),
      constants.Form.TRANSACTION_AMOUNT -> number(min = constants.FormConstraint.TRANSACTION_AMOUNT_MINIMUM_VALUE, max = constants.FormConstraint.TRANSACTION_AMOUNT_MAXIMUM_VALUE),
    )(Data.apply)(Data.unapply)
  )

  case class Data(transactionID: String, transactionAmount: Int)

}
