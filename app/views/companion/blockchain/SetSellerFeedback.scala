package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object SetSellerFeedback {
  val form = Form(
    mapping(
      "from" -> nonEmptyText,
      "password" -> nonEmptyText,
      "to" -> nonEmptyText,
      "pegHash" -> nonEmptyText,
      "rating" -> number,
      "chainID" -> nonEmptyText,
      "gas" -> number
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, to: String, pegHash: String, rating: Int, chainID: String, gas: Int)

}