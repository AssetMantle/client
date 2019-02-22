package views.companion.master

import play.api.data.Form
import play.api.data.Forms._


object AddOrganization {

  val form = Form(
    mapping(
      "name" -> nonEmptyText(minLength = 8, maxLength = 50),
      "address" -> nonEmptyText(minLength = 8, maxLength = 50),
      "phone" -> nonEmptyText(minLength = 8, maxLength = 50),
      "email" -> nonEmptyText(minLength = 8, maxLength = 50)

    )(Data.apply)(Data.unapply)
  )

  case class Data(name: String, address: String, phone: String, email: String)

}
