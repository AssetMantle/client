package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object NotificationBox {
  val form = Form(
    mapping(
      "pageNumber" -> number,
      "notificationID" -> nonEmptyText

    )(Data.apply)(Data.unapply)
  )

  case class Data(pageNumber: Int, notificationID: String)

}
