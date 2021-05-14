package views.companion.master

import play.api.data.Form
import play.api.data.Forms._
import utilities.MicroNumber

object MakeOrder {
  val form = Form(
    mapping(
      constants.FormField.NEGOTIATION_ID.name -> constants.FormField.NEGOTIATION_ID.field,
      constants.FormField.FIAT_PROOF.name -> constants.FormField.FIAT_PROOF.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(negotiationID: String,fiatProof: String, gas: MicroNumber, password: String)

}
