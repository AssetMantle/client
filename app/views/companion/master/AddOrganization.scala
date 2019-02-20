package views.companion.master

import play.api.data.Form
import play.api.data.Forms._


object AddOrganization {

  val form = Form(
    mapping(
      "id" -> nonEmptyText(minLength = 4, maxLength = 20),
      "password" -> nonEmptyText(minLength = 8, maxLength = 50),
      "name" -> nonEmptyText(minLength = 8, maxLength = 50),
      "address" -> nonEmptyText(minLength = 8, maxLength = 50),
      "phone" -> nonEmptyText(minLength = 8, maxLength = 50),
      "email" -> nonEmptyText(minLength = 8, maxLength = 50)

    )(Data.apply)(Data.unapply)
  )

  case class Data(id: String, password: String, name: String, address: String, phone: String, email: String)

}
