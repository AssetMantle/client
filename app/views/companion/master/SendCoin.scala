package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SendCoin {
  val form = Form(
    mapping(
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.AMOUNT.name -> constants.FormField.AMOUNT.field,
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(to: String, amount: Int, password: String)
}
