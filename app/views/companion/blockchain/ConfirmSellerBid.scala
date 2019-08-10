package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object ConfirmSellerBid {
  val form = Form(
    mapping(
      constants.Form.FROM -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.TO -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.BID -> number(min = 1, max = 10000),
      constants.Form.TIME -> number(min = 1, max = 10000),
      constants.Form.PEG_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.SELLER_CONTRACT_HASH -> nonEmptyText(minLength = 5, maxLength = 40),
      constants.Form.MODE-> nonEmptyText(minLength = 4, maxLength = 5)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, to: String, bid: Int, time: Int, pegHash: String, sellerContractHash: String, mode: String)

}
