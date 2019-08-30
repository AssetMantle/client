package views.companion.master

import play.api.data.Form
import play.api.data.Forms._


object AddOrganization {

  val form = Form(
    mapping(
      constants.FormField.ZONE_ID.name -> constants.FormField.ZONE_ID.field,
      constants.FormField.NAME.name -> constants.FormField.NAME.field,
      constants.FormField.ADDRESS.name -> constants.FormField.ADDRESS.field,
      constants.FormField.MOBILE_NUMBER.name -> constants.FormField.MOBILE_NUMBER.field,
      constants.FormField.EMAIL_ADDRESS.name -> constants.FormField.EMAIL_ADDRESS.field

    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, name: String, address: String, mobileNumber: String, emailAddress: String)

}
