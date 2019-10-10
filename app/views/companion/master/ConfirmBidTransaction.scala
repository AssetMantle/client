package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ConfirmBidTransaction {
  val form = Form(
    mapping(
      constants.FormField.REQUEST_ID.name -> constants.FormField.REQUEST_ID.field,
      constants.FormField.TIME.name -> constants.FormField.TIME.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, time: Int, gas: Int, password: String)
}