package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object Login {
  val form = Form(
    mapping(
      constants.Forms.USERNAME -> nonEmptyText(minLength = 4, maxLength = 20),
      constants.Forms.PASSWORD -> nonEmptyText(minLength = 8, maxLength = 50),
      constants.Forms.NOTIFICATION_TOKEN -> nonEmptyText(minLength = 1, maxLength = 180)
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String, password: String, notificationToken: String)

}
