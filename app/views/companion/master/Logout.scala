package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping}

object Logout {
  val form = Form(
    mapping(
      constants.Forms.RECEIVE_NOTIFICATIONS -> boolean,
    )(Data.apply)(Data.unapply)
  )

  case class Data(receiveNotifications: Boolean)

}
