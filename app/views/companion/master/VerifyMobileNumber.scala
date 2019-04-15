package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object VerifyMobileNumber {


  val form = Form(
    mapping(
      constants.Form.OTP -> nonEmptyText(minLength = 6, maxLength = 6),
    )(Data.apply)(Data.unapply)
  )

  case class Data(otp: String)

}
