package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object BuyerExecuteOrder {
  val form = Form(
    mapping(
      "from" -> nonEmptyText,
      "password" -> nonEmptyText,
      "buyerAddress" -> nonEmptyText,
      "sellerAddress" -> nonEmptyText,
      "fiatProofHash" -> nonEmptyText,
      "pegHash" -> nonEmptyText,
      "chainID" -> nonEmptyText,
      "gas" -> number
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, chainID: String, gas: Int)

}
