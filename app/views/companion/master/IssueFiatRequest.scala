package views.companion.master

import play.api.data.Form
import play.api.data.Forms._
import utilities.MicroNumber

object IssueFiatRequest {
  val form = Form(
    mapping(
      constants.FormField.TRANSACTION_AMOUNT.name -> constants.FormField.TRANSACTION_AMOUNT.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(transactionAmount: MicroNumber)

}
