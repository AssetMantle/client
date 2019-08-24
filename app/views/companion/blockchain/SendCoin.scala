package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object SendCoin {
  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.AMOUNT.name -> constants.FormField.AMOUNT.field,
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
      constants.FormField.MODE.name -> constants.FormField.MODE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, amount: Int, password: String, mode: String)
}
