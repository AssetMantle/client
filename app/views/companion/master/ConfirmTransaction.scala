package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ConfirmTransaction {
  val form = Form(
    mapping(
      constants.FormField.REQUEST_ID.name -> constants.FormField.REQUEST_ID.field,
      constants.FormField.GAS.name -> optional(constants.FormField.GAS.field),
      constants.FormField.PASSWORD.name -> optional(constants.FormField.PASSWORD.field),
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, gas: Option[Int], password: Option[String])
}