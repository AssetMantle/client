package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object Logout {
  val form = Form(
    mapping(
      constants.FormField.RECEIVE_NOTIFICATIONS.name -> constants.FormField.RECEIVE_NOTIFICATIONS.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(receiveNotifications: Boolean)

}
