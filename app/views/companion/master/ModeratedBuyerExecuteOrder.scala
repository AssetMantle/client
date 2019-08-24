package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ModeratedBuyerExecuteOrder {

  val form = Form(
    mapping(
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
      constants.FormField.BUYER_ADDRESS.name -> constants.FormField.BUYER_ADDRESS.field,
      constants.FormField.SELLER_ADDRESS.name -> constants.FormField.SELLER_ADDRESS.field,
      constants.FormField.FIAT_PROOF_HASH.name -> constants.FormField.FIAT_PROOF_HASH.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int)

}
