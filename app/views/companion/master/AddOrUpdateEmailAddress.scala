package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object AddOrUpdateEmailAddress {

  val form = Form(
    mapping(
      constants.FormField.EMAIL_ADDRESS.name -> constants.FormField.EMAIL_ADDRESS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(emailAddress: String)

}
