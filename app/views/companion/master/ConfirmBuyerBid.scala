package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ConfirmBuyerBid {
  val form = Form(
    mapping(
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.SELLER_ADDRESS -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.BID -> number(min = 1, max = 10000),
      constants.Form.TIME -> number(min = 1, max = 10000),
      constants.Form.PEG_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.BUYER_CONTRACT_HASH -> nonEmptyText(minLength = 5, maxLength = 40),
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, sellerAddress: String, bid: Int, time: Int, pegHash: String, buyerContractHash: String)

}
