package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object ForgotPassword {
  val form = Form(
    mapping(
      constants.FormField.USERNAME.name -> constants.FormField.USERNAME.field,
      constants.FormField.MNEMONICS.name -> constants.FormField.MNEMONICS.field,
      constants.FormField.OTP.name -> constants.FormField.OTP.field,
      constants.FormField.NEW_PASSWORD.name -> constants.FormField.NEW_PASSWORD.field,
      constants.FormField.CONFIRM_NEW_PASSWORD.name -> constants.FormField.CONFIRM_NEW_PASSWORD.field
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.forgotPasswordConstraint)
  )

  case class Data(username: String, mnemonic: String, otp: String, newPassword: String, confirmNewPassword: String)

}
