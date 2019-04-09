package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ApproveFaucetRequest {

  val form = Form(
    mapping(
      constants.Forms.REQUEST_ID -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Forms.ACCOUNT_ID -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Forms.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.GAS -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, accountID: String, password: String, gas: Int)

}
