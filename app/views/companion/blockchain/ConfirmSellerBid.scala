package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object ConfirmSellerBid {
  val form = Form(
    mapping(
      constants.Forms.FROM -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.TO -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Forms.BID -> number(min = 1, max = 10000),
      constants.Forms.TIME -> number(min = 1, max = 10000),
      constants.Forms.PEG_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.GAS -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, to: String, bid: Int, time: Int, pegHash: String, gas: Int)

}
