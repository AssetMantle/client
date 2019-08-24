package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object RedeemFiat {
  val form = Form(
    mapping(
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
      constants.FormField.ZONE_ID.name -> constants.FormField.ZONE_ID.field,
      constants.FormField.REDEEM_AMOUNT.name -> constants.FormField.REDEEM_AMOUNT.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, zoneID: String, redeemAmount: Int)

}
