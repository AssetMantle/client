package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ConfirmSellerBid {
  val form = Form(
    mapping(
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
      constants.Form.BUYER_ADDRESS -> constants.FormField.BLOCKCHAIN_ADDRESS.field,
      constants.Form.BID -> constants.FormField.BID.field,
      constants.Form.TIME -> constants.FormField.BLOCK_TIME.field,
      constants.Form.PEG_HASH -> constants.FormField.PEG_HASH.field,
      constants.Form.SELLER_CONTRACT_HASH -> nonEmptyText(minLength = 40, maxLength = 40),
      constants.Form.GAS -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, buyerAddress: String, bid: Int, time: Int, pegHash: String, sellerContractHash: String, gas: Int)

}
