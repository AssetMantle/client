package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SetSellerFeedback {
  val form = Form(
    mapping(
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "to" -> nonEmptyText(minLength = 1, maxLength = 45),
      "pegHash" -> nonEmptyText(minLength = 1, maxLength = 20),
      "rating" -> number(min = 1, max = 10000),
      "gas" -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, to: String, pegHash: String, rating: Int, gas: Int)

}