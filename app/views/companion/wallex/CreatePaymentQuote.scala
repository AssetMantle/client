package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

object CreatePaymentQuote {
  val form = Form(
    mapping(
      constants.FormField.WALLEX_CURRENCIES_SELL.name -> constants.FormField.WALLEX_CURRENCIES_SELL.field,
      constants.FormField.WALLEX_CURRENCIES_BUY.name -> constants.FormField.WALLEX_CURRENCIES_SELL.field,
      constants.FormField.AMOUNT_WALLEX.name -> constants.FormField.AMOUNT_WALLEX.field,
      constants.FormField.BENEFICIARY_ID.name -> constants.FormField.BENEFICIARY_ID.field,
      constants.FormField.NEGOTIATION_ID.name -> constants.FormField.NEGOTIATION_ID.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(
      sellCurrency: String,
      buyCurrency: String,
      amount: Double,
      beneficiaryId: String,
      negotiationID: String
  )

}
