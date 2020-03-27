package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object InviteSalesQuoteBuyer {

  val form = Form(
    mapping(
      constants.FormField.REQUEST_ID.name -> constants.FormField.REQUEST_ID.field,
      constants.FormField.COUNTER_PARTY.name -> constants.FormField.COUNTER_PARTY.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String,counterParty:String)

}
