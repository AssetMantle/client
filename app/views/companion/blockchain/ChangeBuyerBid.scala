package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object ChangeBuyerBid {
  val form = Form(
    mapping(
      "from" -> nonEmptyText,
      "password" -> nonEmptyText,
      "to" -> nonEmptyText,
      "bid" -> number,
      "time" -> number,
      "pegHash" -> nonEmptyText,
      "chainID" -> nonEmptyText,
      "gas" -> number
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, to: String, bid: Int, time: Int, pegHash: String, chainID: String, gas: Int)

}
