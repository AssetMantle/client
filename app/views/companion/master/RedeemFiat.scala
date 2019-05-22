package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object RedeemFiat {
  val form = Form(
    mapping(
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ZONE_ID -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.REDEEM_AMOUNT -> number(min = 1, max = 10000),
      constants.Form.GAS -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, zoneID: String, redeemAmount: Int, gas: Int)

}
