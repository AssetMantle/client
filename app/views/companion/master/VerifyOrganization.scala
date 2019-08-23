package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object VerifyOrganization {


  val form = Form(
    mapping(
      constants.Form.ORGANIZATION_ID -> constants.FormField.ORGANIZATION_ID.field,
      constants.Form.ZONE_ID -> constants.FormField.ZONE_ID.field,
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(organizationID: String, zoneID: String, password: String)

}
