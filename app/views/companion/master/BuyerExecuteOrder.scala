package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object BuyerExecuteOrder {
  val form = Form(
    mapping(
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "sellerAddress" -> nonEmptyText(minLength = 1, maxLength = 45),
      "fiatProofHash" -> nonEmptyText(minLength = 1, maxLength = 20),
      "pegHash" -> nonEmptyText(minLength = 1, maxLength = 20),
      "gas" -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int)

}
