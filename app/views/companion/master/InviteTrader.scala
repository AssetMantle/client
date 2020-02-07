package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object InviteTrader {

  val form = Form(
    mapping(
      constants.FormField.EMAIL_ADDRESS.name -> constants.FormField.EMAIL_ADDRESS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(emailAddress: String)

}
