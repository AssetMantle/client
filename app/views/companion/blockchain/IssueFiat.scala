package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, _}

object IssueFiat {
  val form = Form(
    mapping(
      "from" -> nonEmptyText(minLength = 1, maxLength = 20),
      "to" -> nonEmptyText(minLength = 1, maxLength = 45),
      "transactionID" -> nonEmptyText(minLength = 1, maxLength = 20),
      "transactionAmount" -> number(min = 1, max = 10000),
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "gas" -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, transactionID: String, transactionAmount: Int, password: String, gas: Int)

}