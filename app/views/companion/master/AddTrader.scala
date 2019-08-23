package views.companion.master

import play.api.data.Form
import play.api.data.Forms._


object AddTrader {

  val form = Form(
    mapping(
      constants.FormField.ZONE_ID.name -> constants.FormField.ZONE_ID.field,
      constants.FormField.ORGANIZATION_ID.name -> constants.FormField.ORGANIZATION_ID.field,
      constants.FormField.NAME.name -> constants.FormField.NAME.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, organizationID: String, name: String)

}
