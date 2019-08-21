package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object RejectVerifyTraderRequest {

  val form = Form(
    mapping(
      constants.Form.TRADER_ID -> nonEmptyText(minLength = constants.FormConstraint.TRADER_ID_MINIMUM_LENGTH, maxLength = constants.FormConstraint.TRADER_ID_MAXIMUM_LENGTH)
    )(Data.apply)(Data.unapply)
  )

  case class Data(traderID: String)

}
