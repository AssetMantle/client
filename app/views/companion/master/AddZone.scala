package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object AddZone {
  val form = Form(
    mapping(
      constants.FormField.NAME.name -> constants.FormField.NAME.field,
      constants.FormField.CURRENCY.name -> constants.FormField.CURRENCY.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(name: String, currency: String)

}
