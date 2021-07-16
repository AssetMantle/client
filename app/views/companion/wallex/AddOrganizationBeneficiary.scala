package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

object AddOrganizationBeneficiary {

  val form = Form(
    mapping(
      constants.FormField.WALLEX_COUNTRY_CODE.name -> constants.FormField.WALLEX_COUNTRY_CODE.field,
      constants.FormField.WALLEX_COMPANY_ADDRESS.name -> constants.FormField.WALLEX_COMPANY_ADDRESS.field,
      constants.FormField.CITY.name -> constants.FormField.CITY.field,
      constants.FormField.WALLEX_US_POSTAL_CODE.name -> constants.FormField.WALLEX_US_POSTAL_CODE.field,
      constants.FormField.WALLEX_STATE_OR_PROVINCE.name -> constants.FormField.WALLEX_STATE_OR_PROVINCE.field,
      constants.FormField.NICK_NAME.name -> constants.FormField.NICK_NAME.field,
      constants.FormField.WALLEX_ACCOUNT_TYPE.name -> constants.FormField.WALLEX_ACCOUNT_TYPE.field,
      constants.FormField.COMPANY_NAME.name -> constants.FormField.COMPANY_NAME.field,
      constants.FormField.WALLEX_BANK_DETAILS.name -> mapping(
        constants.FormField.BANK_NAME.name -> constants.FormField.BANK_NAME.field,
        constants.FormField.WALLEX_BANK_ADDRESS.name -> constants.FormField.WALLEX_BANK_ADDRESS.field,
        constants.FormField.WALLEX_COUNTRY_CODE.name -> constants.FormField.WALLEX_COUNTRY_CODE.field,
        constants.FormField.ACCOUNT_HOLDER_NAME.name -> constants.FormField.ACCOUNT_HOLDER_NAME.field,
        constants.FormField.ACCOUNT_NUMBER.name -> constants.FormField.ACCOUNT_NUMBER.field,
        constants.FormField.SWIFT_CODE.name -> constants.FormField.SWIFT_CODE.field,
        constants.FormField.WALLEX_ABA.name -> constants.FormField.WALLEX_ABA.field,
        constants.FormField.WALLEX_CURRENCIES.name -> constants.FormField.WALLEX_CURRENCIES.field
      )(BankData.apply)(BankData.unapply)
    )(Data.apply)(Data.unapply)
  )

  case class BankData(
      bankName: String,
      address: String,
      country: String,
      accountHolderName: String,
      accountNumber: String,
      bicSwift: String,
      aba: String,
      currency: String
  )
  case class Data(
      country: String,
      address: String,
      city: String,
      stateOrProvince: String,
      postcode: String,
      nickName: String,
      entityType: String,
      companyName: String,
      bankData: BankData
  )

}
