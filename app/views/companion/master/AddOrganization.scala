package views.companion.master

import play.api.data.Form
import play.api.data.Forms._


object AddOrganization {

  val form = Form(
    mapping(
      constants.Form.ZONE_ID -> constants.FormField.ZONE_ID.field,
      constants.Form.NAME -> constants.FormField.NAME.field,
      constants.Form.ADDRESS -> nonEmptyText(minLength = 8, maxLength = 150),
      constants.Form.PHONE -> constants.FormField.PHONE.field,
      constants.Form.EMAIL -> email

    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, name: String, address: String, phone: String, email: String)

}
