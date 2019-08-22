package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SignUp {

  val form = Form(
    mapping(
      constants.Form.USERNAME -> nonEmptyText(minLength = constants.FormConstraint.USERNAME_MINIMUM_LENGTH, maxLength = constants.FormConstraint.USERNAME_MAXIMUM_LENGTH).verifying(constants.FormConstraint.usernameCheckConstraint),
      constants.Form.USERNAME_AVAILABLE -> boolean,
      constants.Form.PASSWORD -> nonEmptyText(minLength = constants.FormConstraint.PASSWORD_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH).verifying(constants.FormConstraint.passwordCheckConstraint),
      constants.Form.CONFIRM_PASSWORD -> nonEmptyText(minLength = constants.FormConstraint.PASSWORD_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH)
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.signUpCheckConstraint))

  case class Data(username: String, usernameAvailable: Boolean, password: String, confirmPassword: String)

}
