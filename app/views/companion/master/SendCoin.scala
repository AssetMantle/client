package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SendCoin {
  val form = Form(
    mapping(
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.AMOUNT.name -> constants.FormField.AMOUNT.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(to: String, amount: Int, password: String, gas: Int)
}
