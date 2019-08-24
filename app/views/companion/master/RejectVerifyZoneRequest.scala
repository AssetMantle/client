package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object RejectVerifyZoneRequest {

  val form = Form(
    mapping(
      constants.FormField.ZONE_ID.name -> constants.FormField.ZONE_ID.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String)

}
