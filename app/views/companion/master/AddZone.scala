package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object AddZone {
  val form = Form(
    mapping(
      constants.Form.NAME -> constants.FormField.NAME.field,
      constants.Form.CURRENCY -> constants.FormField.CURRENCY.field

    )(Data.apply)(Data.unapply)
  )

  case class Data(name: String, currency: String)

}
