package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber

object SendCoin {
  val form: Form[Data] = Form(
    mapping(
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.DENOM.name -> constants.FormField.DENOM.field,
      constants.FormField.AMOUNT.name -> constants.FormField.AMOUNT.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(to: String, denom: String, amount: MicroNumber)

}
