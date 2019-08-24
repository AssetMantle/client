package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.mapping

object IssueFiat {
  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.TRANSACTION_ID.name -> constants.FormField.TRANSACTION_ID.field,
      constants.FormField.TRANSACTION_AMOUNT.name -> constants.FormField.TRANSACTION_AMOUNT.field,
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, transactionID: String, transactionAmount: Int, password: String, gas: Int)

}