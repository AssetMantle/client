package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, _}

object IssueFiat {
  val form = Form(
    mapping(
      "from" -> nonEmptyText,
      "to" -> nonEmptyText,
      "transactionID" -> nonEmptyText,
      "transactionAmount" -> number,
      "chainID" -> nonEmptyText,
      "password" -> nonEmptyText,
      "gas" -> number
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, transactionID: String, transactionAmount: Int, chainID: String, password: String, gas: Int)

}