package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ModeratedSellerExecuteOrder {
  val form = Form(
    mapping(
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
      constants.FormField.BUYER_ADDRESS.name -> constants.FormField.BUYER_ADDRESS.field,
      constants.FormField.SELLER_ADDRESS.name -> constants.FormField.SELLER_ADDRESS.field,
      constants.FormField.AWB_PROOF_HASH.name -> constants.FormField.AWB_PROOF_HASH.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, buyerAddress: String, sellerAddress: String, awbProofHash: String, pegHash: String, gas: Int)
}
