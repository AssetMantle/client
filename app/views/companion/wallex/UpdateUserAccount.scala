package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

import java.util.Date

object UpdateUserAccount {

  val form = Form(
    mapping(
      constants.FormField.COUNTRY_CODE.name -> constants.FormField.COUNTRY_CODE.field,
      constants.FormField.MOBILE_NUMBER.name -> constants.FormField.MOBILE_NUMBER.field,
      constants.FormField.WALLEX_GENDER.name -> constants.FormField.WALLEX_GENDER.field,
      constants.FormField.WALLEX_COUNTRY_OF_BIRTH.name -> constants.FormField.WALLEX_COUNTRY_OF_BIRTH.field,
      constants.FormField.WALLEX_NATIONALITY.name -> constants.FormField.WALLEX_NATIONALITY.field,
      constants.FormField.WALLEX_COUNTRY_OF_RESIDENCE.name -> constants.FormField.WALLEX_COUNTRY_OF_RESIDENCE.field,
      constants.FormField.WALLEX_RESIDENTIAL_ADDRESS.name -> constants.FormField.WALLEX_RESIDENTIAL_ADDRESS.field,
      constants.FormField.WALLEX_COUNTRY_CODE.name -> constants.FormField.WALLEX_COUNTRY_CODE.field,
      constants.FormField.WALLEX_POSTAL_ZIP_CODE.name -> constants.FormField.WALLEX_POSTAL_ZIP_CODE.field,
      constants.FormField.DATE_OF_BIRTH.name -> constants.FormField.DATE_OF_BIRTH.field,
      constants.FormField.WALLEX_IDENTIFICATION_TYPE.name -> constants.FormField.WALLEX_IDENTIFICATION_TYPE.field,
      constants.FormField.WALLEX_IDENTIFICATION_NUMBER.name -> constants.FormField.WALLEX_IDENTIFICATION_NUMBER.field,
      constants.FormField.WALLEX_ISSUE_DATE.name -> constants.FormField.WALLEX_ISSUE_DATE.field,
      constants.FormField.WALLEX_EXPIRY_DATE.name -> constants.FormField.WALLEX_EXPIRY_DATE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(
      mobileCountryCode: String,
      mobileNumber: String,
      gender: String,
      countryOfBirth: String,
      nationality: String,
      countryOfResidence: String,
      residentialAddress: String,
      countryCode: String,
      postalCode: String,
      dateOfBirth: Date,
      identificationType: String,
      identificationNumber: String,
      issueDate: Date,
      expiryDate: Date
  )

}
