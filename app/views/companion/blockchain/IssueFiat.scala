package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber
import utilities.MicroNumber

object IssueFiat {
  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.TRANSACTION_ID.name -> constants.FormField.TRANSACTION_ID.field,
      constants.FormField.TRANSACTION_AMOUNT.name -> constants.FormField.TRANSACTION_AMOUNT.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.MODE.name -> constants.FormField.MODE.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, transactionID: String, transactionAmount: MicroNumber, gas: MicroNumber, mode: String, password: String)

}