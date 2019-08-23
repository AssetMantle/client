package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SendCoin {
  val form = Form(
    mapping(
      constants.Form.TO -> constants.FormField.BLOCKCHAIN_ADDRESS.field,
      constants.Form.AMOUNT -> constants.FormField.AMOUNT.field,
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
      constants.Form.GAS -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(to: String, amount: Int, password: String, gas: Int)
}
