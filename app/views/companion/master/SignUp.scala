package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SignUp {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.USERNAME.name -> constants.FormField.USERNAME.field,
      constants.FormField.PUBLIC_KEY_TYPE.name -> constants.FormField.PUBLIC_KEY_TYPE.field,
      constants.FormField.PUBLIC_KEY.name -> constants.FormField.PUBLIC_KEY.field,
      constants.FormField.SIGNATURE.name -> constants.FormField.SIGNATURE.field,
      constants.FormField.PUSH_NOTIFICATION_TOKEN.name -> constants.FormField.PUSH_NOTIFICATION_TOKEN.field
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.signUpConstraint))

  case class Data(username: String, publicKeyType: String, publicKey: String, signature: String, pushNotificationToken: String)

}
