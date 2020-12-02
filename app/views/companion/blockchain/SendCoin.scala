package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._
import utilities.MicroNumber

object SendCoin {
  val form = Form(
    mapping(
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.AMOUNT.name -> constants.FormField.AMOUNT.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.MODE.name -> constants.FormField.MODE.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(to: String, amount: MicroNumber, gas: MicroNumber, mode: String, password: String)

}
