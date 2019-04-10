package views.companion.master

import play.api.data.Form
import play.api.data.Forms._


object AddOrganization {

  val form = Form(
    mapping(
      constants.Forms.ZONE_ID -> nonEmptyText(minLength = 8, maxLength = 50),
      constants.Forms.NAME -> nonEmptyText(minLength = 8, maxLength = 50),
      constants.Forms.ADDRESS -> nonEmptyText(minLength = 8, maxLength = 50),
      constants.Forms.PHONE -> nonEmptyText(minLength = 8, maxLength = 50),
      constants.Forms.EMAIL -> nonEmptyText(minLength = 8, maxLength = 50)

    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, name: String, address: String, phone: String, email: String)

}
