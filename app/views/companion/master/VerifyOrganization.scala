package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object VerifyOrganization {


  val form = Form(
    mapping(
      constants.Form.ORGANIZATION_ID -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ZONE_ID -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.PASSWORD -> nonEmptyText(minLength = 4, maxLength = 20),
    )(Data.apply)(Data.unapply)
  )

  case class Data(organizationID: String, zoneID: String, password: String)

}
