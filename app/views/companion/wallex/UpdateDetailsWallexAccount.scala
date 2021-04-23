package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

import java.util.Date

object UpdateDetailsWallexAccount {

  val form = Form(
    mapping(
      constants.FormField.COUNTRY_OF_INCORPORATION.name -> constants.FormField.COUNTRY_OF_INCORPORATION.field,
      constants.FormField.COUNTRY_OF_OPERATIONS.name -> constants.FormField.COUNTRY_OF_OPERATIONS.field,
      constants.FormField.BUSINESS_TYPE.name -> constants.FormField.BUSINESS_TYPE.field,
      constants.FormField.COMPANY_ADDRESS.name -> constants.FormField.COMPANY_ADDRESS.field,
      constants.FormField.POSTAL_ZIP_CODE.name -> constants.FormField.POSTAL_ZIP_CODE.field,
      constants.FormField.STATE_OR_PROVINCE.name -> constants.FormField.STATE_OR_PROVINCE.field,
      constants.FormField.CITY.name -> constants.FormField.CITY.field,
      constants.FormField.REGISTRATION_NUMBER.name -> constants.FormField.REGISTRATION_NUMBER.field,
      constants.FormField.INCORPORATION_DATE.name -> constants.FormField.INCORPORATION_DATE.field
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
