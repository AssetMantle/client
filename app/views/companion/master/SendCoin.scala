package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SendCoin {
  val form = Form(
    mapping(
      constants.Form.TO -> nonEmptyText(minLength = constants.FormConstraint.BLOCKCHAIN_ADDRESS_LENGTH, maxLength = constants.FormConstraint.BLOCKCHAIN_ADDRESS_LENGTH),
      constants.Form.AMOUNT -> number(min = constants.FormConstraint.AMOUNT_MINIMUM_VALUE, max = constants.FormConstraint.AMOUNT_MAXIMUM_VALUE),
      constants.Form.PASSWORD -> nonEmptyText(minLength = constants.FormConstraint.PASSWORD_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH),
      constants.Form.GAS -> number(min = constants.FormConstraint.GAS_MINIMUM_VALUE, max = constants.FormConstraint.GAS_MAXIMUM_VALUE)
    )(Data.apply)(Data.unapply)
  )

  case class Data(to: String, amount: Int, password: String, gas: Int)
}
