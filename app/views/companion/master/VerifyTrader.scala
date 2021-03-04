package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber

object VerifyTrader {
  val form = Form(
    mapping(
      constants.FormField.ACCOUNT_ID.name -> constants.FormField.ACCOUNT_ID.field,
      constants.FormField.ORGANIZATION_ID.name -> constants.FormField.ORGANIZATION_ID.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(accountID: String, organizationID: String, gas: MicroNumber, password: String)

}
