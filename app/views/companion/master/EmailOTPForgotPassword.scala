package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object EmailOTPForgotPassword {
  val form = Form(
    mapping(
      constants.FormField.USERNAME.name -> constants.FormField.USERNAME.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String)

}
