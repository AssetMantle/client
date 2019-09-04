package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object SellerExecuteOrder {
  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
      constants.FormField.BUYER_ADDRESS.name -> constants.FormField.BUYER_ADDRESS.field,
      constants.FormField.SELLER_ADDRESS.name -> constants.FormField.SELLER_ADDRESS.field,
      constants.FormField.AWB_PROOF_HASH.name -> constants.FormField.AWB_PROOF_HASH.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.MODE.name -> constants.FormField.MODE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, buyerAddress: String, sellerAddress: String, awbProofHash: String, pegHash: String, gas:Int, mode: String)

}
