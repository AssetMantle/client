package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SendFiat {
  val form = Form(
    mapping(
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.SELLER_ADDRESS -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.AMOUNT -> number(min = 1, max = 10000),
      constants.Form.PEG_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.GAS -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, sellerAddress: String, amount: Int, pegHash: String, gas: Int)

}