package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber

object WalletTransfer {
  val form = Form(
    mapping(
      constants.FormField.WALLEX_ON_BEHALF_OF.name -> constants.FormField.WALLEX_ON_BEHALF_OF.field,
      constants.FormField.WALLEX_RECEIVER_ACCOUNT_ID.name -> constants.FormField.WALLEX_RECEIVER_ACCOUNT_ID.field,
      constants.FormField.WALLEX_AMOUNT.name -> constants.FormField.WALLEX_AMOUNT.field,
      constants.FormField.WALLEX_CURRENCIES.name -> constants.FormField.WALLEX_CURRENCIES.field,
      constants.FormField.WALLEX_PURPOSE_OF_TRANSFER.name -> constants.FormField.WALLEX_PURPOSE_OF_TRANSFER.field,
      constants.FormField.WALLEX_REFERENCE.name -> constants.FormField.WALLEX_REFERENCE.field,
      constants.FormField.WALLEX_REMARKS.name -> constants.FormField.WALLEX_REMARKS.field,
      constants.FormField.NEGOTIATION_ID.name -> constants.FormField.NEGOTIATION_ID.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(
      onBehalfOf: String,
      receiverAccountID: String,
      amount: MicroNumber,
      currency: String,
      purposesOfTransfer: String,
      reference: String,
      remarks: String,
      negotiationID: String
  )

}
