package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object NotificationBox {
  val form = Form(
    mapping(
      constants.Forms.PAGE_NUMBER -> number,
      constants.Forms.NOTIFICATION_ID -> nonEmptyText

    )(Data.apply)(Data.unapply)
  )

  case class Data(pageNumber: Int, notificationID: String)

}
