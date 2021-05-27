package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber

object AcceptPaymentQuote {
  val form = Form(
    mapping(
      constants.FormField.WALLEX_QUOTE_ID.name -> constants.FormField.WALLEX_QUOTE_ID.field,
      constants.FormField.WALLEX_FUNDING_SOURCE.name -> constants.FormField.WALLEX_FUNDING_SOURCE.field,
      constants.FormField.WALLEX_PAYMENT_REFERENCE.name -> constants.FormField.WALLEX_PAYMENT_REFERENCE.field,
      constants.FormField.WALLEX_PURPOSE_OF_TRANSFER.name -> constants.FormField.WALLEX_PURPOSE_OF_TRANSFER.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )
  case class Data(
      quoteID: String,
      fundingSource: String,
      paymentReference: String,
      purposeOfTransfer: String,
      password: String,
      gas: MicroNumber
  )
}
