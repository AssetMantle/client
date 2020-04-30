package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object AddContractContent {

  val form = Form(
    mapping(
      constants.FormField.NEGOTIATION_ID.name -> constants.FormField.NEGOTIATION_ID.field,
      constants.FormField.CONTRACT_NUMBER.name -> constants.FormField.CONTRACT_NUMBER.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(negotiationID: String, contractNumber: String)

}

