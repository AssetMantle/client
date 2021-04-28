package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

object AcceptPaymentQuote {
  val form = Form(
    mapping(
      constants.FormField.QUOTE_ID.name -> constants.FormField.QUOTE_ID.field,
      constants.FormField.FUNDING_SOURCE.name -> constants.FormField.FUNDING_SOURCE.field,
      constants.FormField.PAYMENT_REFERENCE.name -> constants.FormField.PAYMENT_REFERENCE.field,
      constants.FormField.PURPOSE_OF_TRANSFER.name -> constants.FormField.PURPOSE_OF_TRANSFER.field
    )(Data.apply)(Data.unapply)
  )
  case class Data(
      quoteId: String,
      fundingSource: String,
      paymentReference: String,
      purposeOfTransfer: String
  )
}
