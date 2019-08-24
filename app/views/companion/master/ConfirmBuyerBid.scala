package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ConfirmBuyerBid {
  val form = Form(
    mapping(
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
      constants.FormField.SELLER_ADDRESS.name -> constants.FormField.SELLER_ADDRESS.field,
      constants.FormField.BID.name -> constants.FormField.BID.field,
      constants.FormField.TIME.name -> constants.FormField.TIME.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.BUYER_CONTRACT_HASH.name -> constants.FormField.BUYER_CONTRACT_HASH.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, sellerAddress: String, bid: Int, time: Int, pegHash: String, buyerContractHash: String)

}
