package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object ConfirmSellerBid {
  val form = Form(
    mapping(
      "from" -> nonEmptyText(minLength = 1, maxLength = 20),
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "to" -> nonEmptyText(minLength = 1, maxLength = 45),
      "bid" -> number(min = 1, max = 10000),
      "time" -> number(min = 1, max = 10000),
      "pegHash" -> nonEmptyText(minLength = 1, maxLength = 20),
      "chainID" -> nonEmptyText(minLength = 1, maxLength = 20),
      "gas" -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, to: String, bid: Int, time: Int, pegHash: String, chainID: String, gas: Int)

}
