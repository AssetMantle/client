package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueFiatRequest {
  val form = Form(
    mapping(
      constants.Form.TRANSACTION_ID -> constants.FormField.TRANSACTION_ID.field,
      constants.Form.TRANSACTION_AMOUNT -> constants.FormField.TRANSACTION_AMOUNT.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(transactionID: String, transactionAmount: Int)

}
