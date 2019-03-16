package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object BuyerExecuteOrder {
  val form = Form(
    mapping(
      "from" -> nonEmptyText(minLength = 1, maxLength = 20),
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "buyerAddress" -> nonEmptyText(minLength = 1, maxLength = 45),
      "sellerAddress" -> nonEmptyText(minLength = 1, maxLength = 45),
      "fiatProofHash" -> nonEmptyText(minLength = 1, maxLength = 20),
      "pegHash" -> nonEmptyText(minLength = 1, maxLength = 20),
      "gas" -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int)

}
