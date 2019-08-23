package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ConfirmSellerBid {
  val form = Form(
    mapping(
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
      constants.FormField.BUYER_ADDRESS.name -> constants.FormField.BUYER_ADDRESS.field,
      constants.FormField.BID.name -> constants.FormField.BID.field,
      constants.FormField.TIME.name -> constants.FormField.TIME.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.SELLER_CONTRACT_HASH.name -> constants.FormField.SELLER_CONTRACT_HASH.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, buyerAddress: String, bid: Int, time: Int, pegHash: String, sellerContractHash: String, gas: Int)

}
