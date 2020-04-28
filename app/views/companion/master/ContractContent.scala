package views.companion.master

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._

object ContractContent {

  val form = Form(
    mapping(
      constants.FormField.NEGOTIATION_ID.name -> constants.FormField.NEGOTIATION_ID.field,
      constants.FormField.CONTRACT_NUMBER.name -> constants.FormField.CONTRACT_NUMBER.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(negotiationID: String, contractNumber: String)

}

