package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object IssueFiat {
  val form = Form(
    mapping(
      constants.Form.REQUEST_ID -> constants.FormField.REQUEST_ID.field,
      constants.Form.ACCOUNT_ID -> constants.FormField.ACCOUNT_ID.field,
      constants.Form.TRANSACTION_ID -> constants.FormField.TRANSACTION_ID.field,
      constants.Form.TRANSACTION_AMOUNT -> constants.FormField.TRANSACTION_AMOUNT.field,
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
      constants.Form.GAS -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, accountID: String, transactionID: String, transactionAmount: Int, password: String, gas: Int)

}