package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object Login {
  val form = Form(
    mapping(
      constants.Form.USERNAME -> nonEmptyText(minLength = constants.FormConstraint.USERNAME_MINIMUM_LENGTH, maxLength = constants.FormConstraint.USERNAME_MAXIMUM_LENGTH),
      constants.Form.PASSWORD -> nonEmptyText(minLength = constants.FormConstraint.PASSWORD_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH),
      constants.Form.NOTIFICATION_TOKEN -> text(maxLength = constants.FormConstraint.NOTIFICATION_TOKEN_MAXIMUM_LENGTH)
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String, password: String, notificationToken: String)

}
