package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ApproveFaucetRequest {

  val form = Form(
    mapping(
      constants.Form.REQUEST_ID -> constants.FormField.REQUEST_ID.field,
      constants.Form.ACCOUNT_ID -> constants.FormField.ACCOUNT_ID.field,
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
      constants.Form.GAS -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, accountID: String, password: String, gas: Int)

}
