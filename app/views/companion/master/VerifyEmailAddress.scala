package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object VerifyEmailAddress {


  val form = Form(
    mapping(
      constants.Form.OTP -> nonEmptyText(minLength = constants.FormConstraint.OTP_MINIMUM_LENGTH, maxLength = constants.FormConstraint.OTP_MAXIMUM_LENGTH),
    )(Data.apply)(Data.unapply)
  )

  case class Data(otp: String)

}
