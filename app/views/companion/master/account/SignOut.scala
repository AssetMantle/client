package views.companion.master.account

import play.api.data.Form
import play.api.data.Forms.mapping

object SignOut {
  val form: Form[Data] = Form(
    mapping(
      constants.FormField.RECEIVE_NOTIFICATIONS.name -> constants.FormField.RECEIVE_NOTIFICATIONS.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(receiveNotifications: Boolean)

}
