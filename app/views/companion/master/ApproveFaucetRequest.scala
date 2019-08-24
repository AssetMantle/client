package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ApproveFaucetRequest {

  val form = Form(
    mapping(
      constants.FormField.REQUEST_ID.name -> constants.FormField.REQUEST_ID.field,
      constants.FormField.ACCOUNT_ID.name -> constants.FormField.ACCOUNT_ID.field,
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, accountID: String, password: String)

}
