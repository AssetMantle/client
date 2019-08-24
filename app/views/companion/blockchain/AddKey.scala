package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.mapping

object AddKey {
  val form = Form(
    mapping(
      constants.FormField.NAME.name -> constants.FormField.NAME.field,
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(name: String, password: String)

}
