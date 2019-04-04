package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, _}

object IssueFiat {
  val form = Form(
    mapping(
      constants.Forms.TO -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Forms.TRANSACTION_ID -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.TRANSACTION_AMOUNT -> number(min = 1, max = 10000),
      constants.Forms.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.GAS -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(to: String, transactionID: String, transactionAmount: Int, password: String, gas: Int)

}