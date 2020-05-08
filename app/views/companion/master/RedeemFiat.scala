package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object RedeemFiat {
  val form = Form(
    mapping(
      constants.FormField.REDEEM_AMOUNT.name -> constants.FormField.REDEEM_AMOUNT.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(redeemAmount: Int, gas: Int, password: String)

}
