package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ModeratedBuyerExecuteOrder {

  val form = Form(
    mapping(
      constants.FormField.BUYER_ACCOUNT_ID.name -> constants.FormField.BUYER_ACCOUNT_ID.field,
      constants.FormField.SELLER_ACCOUNT_ID.name -> constants.FormField.SELLER_ACCOUNT_ID.field,
      constants.FormField.FIAT_PROOF_HASH.name -> constants.FormField.FIAT_PROOF_HASH.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(buyerAccountID: String, sellerAccountID: String, fiatProofHash: String, pegHash: String, gas: Int, password: String)

}
