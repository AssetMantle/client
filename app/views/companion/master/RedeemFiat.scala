package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object RedeemFiat {
  val form = Form(
    mapping(
      constants.Form.PASSWORD -> nonEmptyText(minLength = constants.FormConstraint.PASSWORD_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH),
      constants.Form.ZONE_ID -> nonEmptyText(minLength = constants.FormConstraint.ZONE_ID_MINIMUM_LENGTH, maxLength = constants.FormConstraint.ZONE_ID_MAXIMUM_LENGTH),
      constants.Form.REDEEM_AMOUNT -> number(min = constants.FormConstraint.REDEEM_AMOUNT_MINIMUM_VALUE, max = constants.FormConstraint.REDEEM_AMOUNT_MAXIMUM_VALUE),
      constants.Form.GAS -> number(min = constants.FormConstraint.GAS_MINIMUM_VALUE, max = constants.FormConstraint.GAS_MAXIMUM_VALUE)
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, zoneID: String, redeemAmount: Int, gas: Int)

}
