package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object Login {
  val form = Form(
    mapping(
      "username" -> nonEmptyText(minLength = 4, maxLength = 20),
      "password" -> nonEmptyText(minLength = 8, maxLength = 50),
      "notificationToken" -> nonEmptyText(minLength = 1, maxLength = 180)
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String, password: String, notificationToken: String)

}
