package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object UpdateContact {


  val form = Form(
    mapping(
      "emailAddress" -> email,
      "mobileNumber" -> nonEmptyText(minLength = 10, maxLength = 10),
      "countryCode" -> nonEmptyText(minLength = 1, maxLength = 5)
    )(Data.apply)(Data.unapply)
  )

  case class Data(emailAddress: String, mobileNumber: String, countryCode: String)

}
