package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

import java.util.Date

object UpdateCompanyAccount {

  val form = Form(
    mapping(
      constants.FormField.WALLEX_COUNTRY_OF_INCORPORATION.name -> constants.FormField.WALLEX_COUNTRY_OF_INCORPORATION.field,
      constants.FormField.WALLEX_COUNTRY_OF_OPERATIONS.name -> constants.FormField.WALLEX_COUNTRY_OF_OPERATIONS.field,
      constants.FormField.WALLEX_BUSINESS_TYPE.name -> constants.FormField.WALLEX_BUSINESS_TYPE.field,
      constants.FormField.WALLEX_COMPANY_ADDRESS.name -> constants.FormField.WALLEX_COMPANY_ADDRESS.field,
      constants.FormField.WALLEX_POSTAL_ZIP_CODE.name -> constants.FormField.WALLEX_POSTAL_ZIP_CODE.field,
      constants.FormField.WALLEX_STATE_OR_PROVINCE.name -> constants.FormField.WALLEX_STATE_OR_PROVINCE.field,
      constants.FormField.CITY.name -> constants.FormField.CITY.field,
      constants.FormField.WALLEX_REGISTRATION_NUMBER.name -> constants.FormField.WALLEX_REGISTRATION_NUMBER.field,
      constants.FormField.WALLEX_INCORPORATION_DATE.name -> constants.FormField.WALLEX_INCORPORATION_DATE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(
      countryOfIncorporation: String,
      countryOfOperations: String,
      businessType: String,
      companyAddress: String,
      postalCode: String,
      state: String,
      city: String,
      registrationNumber: String,
      incorporationDate: Date
  )

}
