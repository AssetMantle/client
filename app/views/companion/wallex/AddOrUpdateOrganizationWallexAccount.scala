package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

object AddOrUpdateOrganizationWallexAccount {

  val form = Form(
    mapping(
      constants.FormField.FIRST_NAME.name -> constants.FormField.FIRST_NAME.field,
      constants.FormField.LAST_NAME.name -> constants.FormField.LAST_NAME.field,
      constants.FormField.EMAIL_ADDRESS.name -> constants.FormField.EMAIL_ADDRESS.field,
      constants.FormField.COUNTRY_CODE2.name -> constants.FormField.COUNTRY_CODE2.field,
      constants.FormField.ACCOUNT_TYPE.name -> constants.FormField.ACCOUNT_TYPE.field
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
