package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object VerifyPassphrase {

  val form = Form(
    mapping(
      constants.FormField.PASSPHRASE_ELEMENT_1.name -> constants.FormField.PASSPHRASE_ELEMENT_1.field,
      constants.FormField.PASSPHRASE_ELEMENT_2.name -> constants.FormField.PASSPHRASE_ELEMENT_2.field,
      constants.FormField.PASSPHRASE_ELEMENT_3.name -> constants.FormField.PASSPHRASE_ELEMENT_3.field,
      constants.FormField.NAME.name -> constants.FormField.NAME.field,
      constants.FormField.BLOCKCHAIN_ADDRESS.name -> constants.FormField.BLOCKCHAIN_ADDRESS.field,
      constants.FormField.PUBLIC_KEY.name -> constants.FormField.PUBLIC_KEY.field,
      constants.FormField.SEED.name -> constants.FormField.SEED.field,
      constants.FormField.PASSPHRASE_ELEMENT_ID_1.name -> constants.FormField.PASSPHRASE_ELEMENT_ID_1.field,
      constants.FormField.PASSPHRASE_ELEMENT_ID_2.name -> constants.FormField.PASSPHRASE_ELEMENT_ID_2.field,
      constants.FormField.PASSPHRASE_ELEMENT_ID_3.name -> constants.FormField.PASSPHRASE_ELEMENT_ID_3.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(passphraseElement1: String, passphraseElement2: String, passphraseElement3: String,name:String,blockchainAddress: String, publicKey: String, seed:String, passphraseElementID1 :Int, passphraseElementID2 :Int, passphraseElementID3 :Int)

}