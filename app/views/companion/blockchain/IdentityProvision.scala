package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber

object IdentityProvision {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.IDENTITY_ID.name -> constants.FormField.IDENTITY_ID.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, identityID: String, to: String, gas: MicroNumber)

}
