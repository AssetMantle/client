package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ChangeSellerBid {
  val form = Form(
    mapping(
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.BUYER_ADDRESS -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.BID -> number(min = 1, max = 10000),
      constants.Form.TIME -> number(min = 1, max = 10000),
      constants.Form.PEG_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, buyerAddress: String, bid: Int, time: Int, pegHash: String)

}
