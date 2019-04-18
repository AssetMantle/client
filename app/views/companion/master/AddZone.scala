package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object AddZone {
  val form = Form(
    mapping(
      constants.Form.NAME -> nonEmptyText(minLength = 8, maxLength = 50),
      constants.Form.CURRENCY -> nonEmptyText(minLength = 8, maxLength = 50)

    )(Data.apply)(Data.unapply)
  )

  case class Data(name: String, currency: String)

}
