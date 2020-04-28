package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object ConfirmAllNegotiationTerms {
  val form = Form(
    mapping(
      constants.FormField.NEGOTIATION_ID.name -> constants.FormField.NEGOTIATION_ID.field,
      constants.FormField.CONFIRM.name -> constants.FormField.CONFIRM.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(negotiationID: String, confirm: Boolean)

}
