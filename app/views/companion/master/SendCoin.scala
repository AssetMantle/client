package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SendCoin {
  val form = Form(
    mapping(
      "to" -> nonEmptyText(minLength = 1, maxLength = 45),
      "amount" -> number(min = 1, max = 1000000),
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "gas" -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(to: String, amount: Int, password: String, gas: Int)
}
