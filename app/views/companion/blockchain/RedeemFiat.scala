package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object RedeemFiat {
  val form = Form(
    mapping(
      "from" -> nonEmptyText,
      "password" -> nonEmptyText,
      "to" -> nonEmptyText,
      "redeemAmount" -> number,
      "chainID" -> nonEmptyText,
      "gas" -> number
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, to: String, redeemAmount: Int, chainID: String, gas: Int)

}
