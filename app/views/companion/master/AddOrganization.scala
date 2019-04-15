package views.companion.master

import play.api.data.Form
import play.api.data.Forms._


object AddOrganization {

  val form = Form(
    mapping(
      constants.Form.ZONE_ID -> nonEmptyText(minLength = 8, maxLength = 50),
      constants.Form.NAME -> nonEmptyText(minLength = 8, maxLength = 50),
      constants.Form.ADDRESS -> nonEmptyText(minLength = 8, maxLength = 50),
      constants.Form.PHONE -> nonEmptyText(minLength = 8, maxLength = 50),
      constants.Form.EMAIL -> nonEmptyText(minLength = 8, maxLength = 50)

    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, name: String, address: String, phone: String, email: String)

}
