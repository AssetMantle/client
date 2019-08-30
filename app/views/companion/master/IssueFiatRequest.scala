package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object IssueFiatRequest {
  val form = Form(
    mapping(
      constants.FormField.TRANSACTION_ID.name -> constants.FormField.TRANSACTION_ID.field,
      constants.FormField.TRANSACTION_AMOUNT.name -> constants.FormField.TRANSACTION_AMOUNT.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(transactionID: String, transactionAmount: Int,gas:Int)

}
