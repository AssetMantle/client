package views.companion.master.account

import play.api.data.Form
import play.api.data.Forms.mapping

object ForgotPassword {
  val form: Form[Data] = Form(
    mapping(
      constants.FormField.USERNAME.name -> constants.FormField.USERNAME.field,
      constants.FormField.MNEMONICS.name -> constants.FormField.MNEMONICS.field,
      constants.FormField.NEW_PASSWORD.name -> constants.FormField.NEW_PASSWORD.field,
      constants.FormField.CONFIRM_NEW_PASSWORD.name -> constants.FormField.CONFIRM_NEW_PASSWORD.field
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.forgotPasswordConstraint)
  )

  case class Data(username: String, mnemonics: String, newPassword: String, confirmNewPassword: String)

}
