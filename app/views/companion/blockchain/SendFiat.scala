package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object SendFiat {
  val form = Form(
    mapping(
      "from" -> nonEmptyText,
      "password" -> nonEmptyText,
      "to" -> nonEmptyText,
      "amount" -> number,
      "pegHash" -> nonEmptyText,
      "chainID" -> nonEmptyText,
      "gas" -> number
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, to: String, amount: Int, pegHash: String, chainID: String, gas: Int)

}