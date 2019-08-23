package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object BuyerExecuteOrder {
  val form = Form(
    mapping(
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
      constants.FormField.SELLER_ADDRESS.name -> constants.FormField.SELLER_ADDRESS.field,
      constants.FormField.FIAT_PROOF_HASH.name -> constants.FormField.FIAT_PROOF_HASH.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int)
}
