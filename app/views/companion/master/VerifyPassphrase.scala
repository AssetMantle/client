package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object VerifyPassphrase {

  val form = Form(
    mapping(
      constants.FormField.PASSPHRASE_ELEMENT.name -> constants.FormField.PASSPHRASE_ELEMENT.field,
      constants.FormField.PASSPHRASE_ELEMENT.name -> constants.FormField.PASSPHRASE_ELEMENT.field,
      constants.FormField.PASSPHRASE_ELEMENT.name -> constants.FormField.PASSPHRASE_ELEMENT.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(passphraseElement1: String, passphraseElement2: String, passphraseElement3: String)

}