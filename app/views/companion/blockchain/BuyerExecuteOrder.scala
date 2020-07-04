package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._
import utilities.MicroLong

object BuyerExecuteOrder {
  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.BUYER_ADDRESS.name -> constants.FormField.BUYER_ADDRESS.field,
      constants.FormField.SELLER_ADDRESS.name -> constants.FormField.SELLER_ADDRESS.field,
      constants.FormField.FIAT_PROOF_HASH.name -> constants.FormField.FIAT_PROOF_HASH.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.MODE.name -> constants.FormField.MODE.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: MicroLong, mode: String, password: String)

}
