package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ModeratedBuyerExecuteOrder {

  val form = Form(
    mapping(
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
      constants.Form.BUYER_ADDRESS -> constants.FormField.BLOCKCHAIN_ADDRESS.field,
      constants.Form.SELLER_ADDRESS -> constants.FormField.BLOCKCHAIN_ADDRESS.field,
      constants.Form.FIAT_PROOF_HASH -> constants.FormField.HASH.field,
      constants.Form.PEG_HASH -> constants.FormField.PEG_HASH.field,
      constants.Form.GAS -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int)

}
