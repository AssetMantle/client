package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object AddOrUpdateOrganizationBankAccountDetail {

  val form = Form(
    mapping(
      constants.FormField.ACCOUNT_HOLDER_NAME.name -> constants.FormField.ACCOUNT_HOLDER_NAME.field,
      constants.FormField.NICK_NAME.name -> constants.FormField.NICK_NAME.field,
      constants.FormField.ACCOUNT_NUMBER.name -> constants.FormField.ACCOUNT_NUMBER.field,
      constants.FormField.BANK_NAME.name -> constants.FormField.BANK_NAME.field,
      constants.FormField.SWIFT_CODE.name -> constants.FormField.SWIFT_CODE.field,
      constants.FormField.STREET_ADDRESS.name -> constants.FormField.STREET_ADDRESS.field,
      constants.FormField.COUNTRY.name -> constants.FormField.COUNTRY.field,
      constants.FormField.ZIP_CODE.name -> constants.FormField.ZIP_CODE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(accountHolder: String, nickName: String, accountNumber: String, bankName: String, swiftAddress: String, streetAddress: String, country: String, zipCode: String)

}
