package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object OrganizationBankAccountDetail {

  val form = Form(
    mapping(
      constants.FormField.ACCOUNT_HOLDER_NAME.name -> constants.FormField.ACCOUNT_HOLDER_NAME.field,
      constants.FormField.NICK_NAME.name -> constants.FormField.NICK_NAME.field,
      constants.FormField.ACCOUNT_NUMBER.name -> constants.FormField.ACCOUNT_NUMBER.field,
      constants.FormField.BANK_NAME.name -> constants.FormField.BANK_NAME.field,
      constants.FormField.SWIFT_ADDRESS.name -> constants.FormField.SWIFT_ADDRESS.field,
      constants.FormField.ADDRESS.name -> constants.FormField.ADDRESS.field,
      constants.FormField.COUNTRY.name -> constants.FormField.COUNTRY.field,
      constants.FormField.ZIP_CODE.name -> constants.FormField.ZIP_CODE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(accountHolder: String, nickName: String, bankAccountNumber: String, bankName: String, swiftAddress: String, address: String, country: String, zipCode: String)
}
