package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object KycRequest {
  val form = Form(
    mapping(
      constants.FormField.EMAIL_ADDRESS.name -> constants.FormField.EMAIL_ADDRESS.field,
      constants.FormField.COUNTER_PARTY.name -> constants.FormField.COUNTER_PARTY.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(eMailId: String, counterParty: String)

}
