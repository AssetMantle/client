package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object ReviewAddZoneRequest {
  val form = Form(
    mapping(
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String)

}
