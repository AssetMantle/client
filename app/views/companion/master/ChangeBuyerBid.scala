package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ChangeBuyerBid {
  val form = Form(
    mapping(
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "to" -> nonEmptyText(minLength = 1, maxLength = 45),
      "bid" -> number(min = 1, max = 10000),
      "time" -> number(min = 1, max = 10000),
      "pegHash" -> nonEmptyText(minLength = 1, maxLength = 20),
      "gas" -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, to: String, bid: Int, time: Int, pegHash: String, gas: Int)

}
