package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object AddOrUpdateContact {

  val form = Form(
    mapping(
      constants.FormField.EMAIL_ADDRESS.name -> constants.FormField.EMAIL_ADDRESS.field,
      constants.FormField.MOBILE_NUMBER.name -> constants.FormField.MOBILE_NUMBER.field,
      constants.FormField.COUNTRY_CODE.name -> constants.FormField.COUNTRY_CODE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(emailAddress: String, mobileNumber: String, countryCode: String)

}
