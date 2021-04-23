package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

object WallexWalletTransfer {
  val form = Form(
    mapping(
      constants.FormField.ON_BEHALF_OF.name -> constants.FormField.ON_BEHALF_OF.field,
      constants.FormField.RECEIVER_ACCOUNT_ID.name -> constants.FormField.RECEIVER_ACCOUNT_ID.field,
      constants.FormField.AMOUNT_WALLEX.name -> constants.FormField.AMOUNT_WALLEX.field,
      constants.FormField.WALLEX_CURRENCIES.name -> constants.FormField.WALLEX_CURRENCIES.field,
      constants.FormField.PURPOSE_OF_TRANSFER.name -> constants.FormField.PURPOSE_OF_TRANSFER.field,
      constants.FormField.WALLEX_REFERENCE.name -> constants.FormField.WALLEX_REFERENCE.field,
      constants.FormField.REMARKS.name -> constants.FormField.REMARKS.field,
      constants.FormField.NEGOTIATION_ID.name -> constants.FormField.NEGOTIATION_ID.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(
      onBehalfOf: String,
      receiverAccountId: String,
      amount: Double,
      currency: String,
      purposesOfTransfer: String,
      reference: String,
      remarks: String,
      negotiationId: String
  )

}
