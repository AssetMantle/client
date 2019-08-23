package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object RedeemFiat {
  val form = Form(
    mapping(
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
      constants.Form.ZONE_ID -> constants.FormField.ZONE_ID.field,
      constants.Form.REDEEM_AMOUNT -> constants.FormField.REDEEM_AMOUNT.field,
      constants.Form.GAS -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, zoneID: String, redeemAmount: Int, gas: Int)

}
