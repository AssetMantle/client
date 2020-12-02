package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber

object IdentityUnprovision {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.IDENTITY_ID.name -> constants.FormField.IDENTITY_ID.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(identityID: String, to: String, gas: MicroNumber, password: String)

}
