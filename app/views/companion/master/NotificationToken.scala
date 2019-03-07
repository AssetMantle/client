package views.companion.master

import play.api.data.Form
import play.api.data.Forms._


object NotificationToken {

  val form = Form(
    mapping(
      "token" -> nonEmptyText(minLength = 1, maxLength = 200)
    )(Data.apply)(Data.unapply)
  )

  case class Data(token: String)

}
