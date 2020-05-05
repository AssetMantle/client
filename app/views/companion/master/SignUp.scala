package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SignUp {

  val form = Form(
    mapping(
      constants.FormField.SIGNUP_USERNAME.name -> constants.FormField.SIGNUP_USERNAME.field,
      constants.FormField.USERNAME_AVAILABLE.name -> constants.FormField.USERNAME_AVAILABLE.field,
      constants.FormField.SIGNUP_PASSWORD.name -> constants.FormField.SIGNUP_PASSWORD.field,
      constants.FormField.SIGNUP_CONFIRM_PASSWORD.name -> constants.FormField.SIGNUP_CONFIRM_PASSWORD.field
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.signUpConstraint))

  case class Data(username: String, usernameAvailable: Boolean, password: String, confirmPassword: String)

}
