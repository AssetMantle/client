package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object SetBuyerFeedback {
  val form = Form(
    mapping(
      "from" -> nonEmptyText(minLength = 1, maxLength = 20),
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "to" -> nonEmptyText(minLength = 1, maxLength = 45),
      "pegHash" -> nonEmptyText(minLength = 1, maxLength = 20),
      "rating" -> number(min = 1, max = 10000),
      "gas" -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, to: String, pegHash: String, rating: Int, gas: Int)

}