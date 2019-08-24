package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object RedeemFiat {
  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.REDEEM_AMOUNT.name -> constants.FormField.REDEEM_AMOUNT.field,
      constants.Form.MODE-> nonEmptyText(minLength = 4, maxLength = 5)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, to: String, redeemAmount: Int, mode: String)

}
