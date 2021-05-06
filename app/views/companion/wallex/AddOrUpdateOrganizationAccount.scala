package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

object AddOrUpdateOrganizationAccount {

  val form = Form(
    mapping(
      constants.FormField.FIRST_NAME.name -> constants.FormField.FIRST_NAME.field,
      constants.FormField.LAST_NAME.name -> constants.FormField.LAST_NAME.field,
      constants.FormField.EMAIL_ADDRESS.name -> constants.FormField.EMAIL_ADDRESS.field,
      constants.FormField.WALLEX_COUNTRY_CODE.name -> constants.FormField.WALLEX_COUNTRY_CODE.field,
      constants.FormField.WALLEX_ACCOUNT_TYPE.name -> constants.FormField.WALLEX_ACCOUNT_TYPE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(
      firstName: String,
      lastName: String,
      email: String,
      countryCode: String,
      accountType: String
  )

}
