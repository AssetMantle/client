package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object UpdateContact {

  val form = Form(
    mapping(
      constants.Form.EMAIL -> email,
      constants.Form.MOBILE_NUMBER -> nonEmptyText(minLength = constants.FormConstraint.MOBILE_NUMBER_MINIMUM_LENGTH, maxLength = constants.FormConstraint.MOBILE_NUMBER_MAXIMUM_LENGTH).verifying(constants.FormConstraint.mobileNumberCheckConstraint),
      constants.Form.COUNTRY_CODE -> nonEmptyText(minLength = constants.FormConstraint.COUNTRY_CODE_MINIMUM_LENGTH, maxLength = constants.FormConstraint.COUNTRY_CODE_MAXIMUM_LENGTH)
    )(Data.apply)(Data.unapply)
  )

  case class Data(emailAddress: String, mobileNumber: String, countryCode: String)

}
