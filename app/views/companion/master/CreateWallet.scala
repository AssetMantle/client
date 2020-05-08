package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object CreateWallet {
  val form = Form(
    mapping(
      constants.FormField.USERNAME.name -> constants.FormField.USERNAME.field,
      constants.FormField.MNEMONICS.name -> constants.FormField.MNEMONICS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String, mnemonics: String, password: String)

}
