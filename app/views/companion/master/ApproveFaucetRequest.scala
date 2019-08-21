package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ApproveFaucetRequest {

  val form = Form(
    mapping(
      constants.Form.REQUEST_ID -> nonEmptyText(minLength = constants.FormConstraint.REQUEST_ID_LENGTH, maxLength = constants.FormConstraint.REQUEST_ID_LENGTH),
      constants.Form.ACCOUNT_ID -> nonEmptyText(minLength = constants.FormConstraint.USERNAME_MINIMUM_LENGTH, maxLength = constants.FormConstraint.USERNAME_MAXIMUM_LENGTH),
      constants.Form.PASSWORD -> nonEmptyText(minLength = constants.FormConstraint.PASSWORD_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH),
      constants.Form.GAS -> number(min = constants.FormConstraint.GAS_MINIMUM_VALUE, max = constants.FormConstraint.GAS_MAXIMUM_VALUE)
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, accountID: String, password: String, gas: Int)

}
