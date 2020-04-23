package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object VerifyEmailAddress {

  val form = Form(
    mapping(
      constants.FormField.OTP.name -> constants.FormField.OTP.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(otp: String)

}
