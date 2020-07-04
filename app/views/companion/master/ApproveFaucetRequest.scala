package views.companion.master

import play.api.data.Form
import play.api.data.Forms._
import utilities.MicroLong

object ApproveFaucetRequest {

  val form = Form(
    mapping(
      constants.FormField.REQUEST_ID.name -> constants.FormField.REQUEST_ID.field,
      constants.FormField.ACCOUNT_ID.name -> constants.FormField.ACCOUNT_ID.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, accountID: String, gas: MicroLong, password: String)

}
