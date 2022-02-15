package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object UpdateProfile {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.NAME.name -> constants.FormField.NAME.field,
      constants.FormField.DESCRIPTION.name -> constants.FormField.DESCRIPTION.field,
    )(Data.apply)(Data.unapply))

  case class Data(name: String, description: String)

}
