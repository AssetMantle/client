package views.companion.master

import play.api.data.Form
import play.api.data.Forms._


object AddOrganization {

  val form = Form(
    mapping(
      constants.Form.ZONE_ID -> nonEmptyText(minLength = constants.FormConstraint.ZONE_ID_MINIMUM_LENGTH, maxLength = constants.FormConstraint.ZONE_ID_MAXIMUM_LENGTH),
      constants.Form.NAME -> nonEmptyText(minLength = constants.FormConstraint.NAME_MINIMUM_LENGTH, maxLength = constants.FormConstraint.NAME_MAXIMUM_LENGTH),
      constants.Form.ADDRESS -> nonEmptyText(minLength = 8, maxLength = 150),
      constants.Form.MOBILE_NUMBER -> nonEmptyText(minLength = constants.FormConstraint.MOBILE_NUMBER_MINIMUM_LENGTH, maxLength = constants.FormConstraint.MOBILE_NUMBER_MAXIMUM_LENGTH).verifying(constants.FormConstraint.mobileNumberCheckConstraint),
      constants.Form.EMAIL -> email

    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, name: String, address: String, phone: String, email: String)

}
