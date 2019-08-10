package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ApproveFaucetRequest {

  val form = Form(
    mapping(
      constants.Form.REQUEST_ID -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.ACCOUNT_ID -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, accountID: String, password: String)

}
