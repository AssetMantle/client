package views.companion.master

import play.api.data.Form
import play.api.data.Forms._


object AddOrganization {

  val form = Form(
    mapping(
      constants.FormField.ZONE_ID.name -> constants.FormField.ZONE_ID.field,
      constants.FormField.NAME.name -> constants.FormField.NAME.field,
      constants.FormField.ADDRESS.name -> constants.FormField.ADDRESS.field,
      constants.FormField.PHONE.name -> constants.FormField.PHONE.field,
      constants.FormField.EMAIL.name -> constants.FormField.EMAIL.field

    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, name: String, address: String, phone: String, email: String)

}
