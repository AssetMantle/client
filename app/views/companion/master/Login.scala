package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object Login {
  val form = Form(
    mapping(
      constants.Form.USERNAME -> constants.FormField.ACCOUNT_ID.field,
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
      constants.Form.NOTIFICATION_TOKEN -> constants.FormField.NOTIFICATION_TOKEN.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String, password: String, notificationToken: String)

}
