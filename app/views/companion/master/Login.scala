package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object Login {
  val form = Form(
    mapping(
      constants.FormField.USERNAME.name -> constants.FormField.USERNAME.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
      constants.FormField.PUSH_NOTIFICATION_TOKEN.name -> constants.FormField.PUSH_NOTIFICATION_TOKEN.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String, password: String, pushNotificationToken: String)

}
