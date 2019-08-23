package views.companion.master

import play.api.data.Form
import play.api.data.Forms._


object AddTrader {

  val form = Form(
    mapping(
      constants.Form.ZONE_ID -> constants.FormField.ZONE_ID.field,
      constants.Form.ORGANIZATION_ID -> constants.FormField.ORGANIZATION_ID.field,
      constants.Form.NAME -> constants.FormField.NAME.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, organizationID: String, name: String)

}
