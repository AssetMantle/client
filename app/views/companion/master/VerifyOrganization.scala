package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object VerifyOrganization {


  val form = Form(
    mapping(
      "organizationID" -> nonEmptyText(minLength = 1, maxLength = 20),
      "zoneID" -> nonEmptyText(minLength = 1, maxLength = 20),
      "password" -> nonEmptyText(minLength = 4, maxLength = 20),
    )(Data.apply)(Data.unapply)
  )

  case class Data(organizationID: String, zoneID: String, password: String)

}
