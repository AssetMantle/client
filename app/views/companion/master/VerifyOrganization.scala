package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object VerifyOrganization {


  val form = Form(
    mapping(
      constants.Form.ORGANIZATION_ID -> nonEmptyText(minLength = constants.FormConstraint.ORGANIZATION_ID_MINIMUM_LENGTH, maxLength = constants.FormConstraint.ORGANIZATION_ID_MAXIMUM_LENGTH),
      constants.Form.ZONE_ID -> nonEmptyText(minLength = constants.FormConstraint.ZONE_ID_MINIMUM_LENGTH, maxLength = constants.FormConstraint.ZONE_ID_MAXIMUM_LENGTH),
      constants.Form.PASSWORD -> nonEmptyText(minLength = constants.FormConstraint.PASSWORD_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH),
    )(Data.apply)(Data.unapply)
  )

  case class Data(organizationID: String, zoneID: String, password: String)

}
