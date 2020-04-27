package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object ChangePassword {
  val form = Form(
    mapping(
      constants.FormField.OLD_PASSWORD.name -> constants.FormField.OLD_PASSWORD.field,
      constants.FormField.NEW_PASSWORD.name -> constants.FormField.NEW_PASSWORD.field,
      constants.FormField.CONFIRM_NEW_PASSWORD.name -> constants.FormField.CONFIRM_NEW_PASSWORD.field
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.changePasswordConstraint)
  )

  case class Data(oldPassword: String, newPassword: String, confirmNewPassword: String)

}
