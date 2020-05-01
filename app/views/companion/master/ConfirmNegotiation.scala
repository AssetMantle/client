package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object ConfirmNegotiation {

  val form = Form(
    mapping(
      constants.FormField.ID.name -> constants.FormField.ID.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: String, gas: Int, password: String)

}
