package views.companion.master

import play.api.data.Form
import play.api.data.Forms._


object AddTrader {

  val form = Form(
    mapping(
      constants.Form.ZONE_ID -> nonEmptyText(minLength = constants.FormConstraint.ZONE_ID_MINIMUM_LENGTH, maxLength = constants.FormConstraint.ZONE_ID_MAXIMUM_LENGTH),
      constants.Form.ORGANIZATION_ID -> nonEmptyText(minLength = constants.FormConstraint.ORGANIZATION_ID_MINIMUM_LENGTH, maxLength = constants.FormConstraint.ORGANIZATION_ID_MAXIMUM_LENGTH),
      constants.Form.NAME -> nonEmptyText(minLength = constants.FormConstraint.NAME_MINIMUM_LENGTH, maxLength = constants.FormConstraint.NAME_MAXIMUM_LENGTH),
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, organizationID: String, name: String)

}
