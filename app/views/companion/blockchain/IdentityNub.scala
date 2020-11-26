package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber

object IdentityNub {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.NUB_ID.name -> constants.FormField.NUB_ID.field,
      constants.FormField.LABEL.name -> constants.FormField.LABEL.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(nubID: String, label: String, gas: MicroNumber, password: String)

}
