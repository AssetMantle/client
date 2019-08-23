package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object VerifyZone {


  val form = Form(
    mapping(
      constants.FormField.ZONE_ID.name -> constants.FormField.ZONE_ID.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, password: String)

}
