package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object AddOrUpdateMobileNumber {

  val form = Form(
    mapping(
      constants.FormField.MOBILE_NUMBER.name -> constants.FormField.MOBILE_NUMBER.field,
      constants.FormField.COUNTRY_CODE.name -> constants.FormField.COUNTRY_CODE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(mobileNumber: String, countryCode: String)

}
