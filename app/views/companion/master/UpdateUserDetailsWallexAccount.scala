package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

import java.util.Date

object UpdateUserDetailsWallexAccount {

  val form = Form(
    mapping(
      constants.FormField.COUNTRY_CODE.name -> constants.FormField.COUNTRY_CODE.field,
      constants.FormField.MOBILE_NUMBER.name -> constants.FormField.MOBILE_NUMBER.field,
      constants.FormField.GENDER.name -> constants.FormField.GENDER.field,
      constants.FormField.COUNTRY_OF_BIRTH.name -> constants.FormField.COUNTRY_OF_BIRTH.field,
      constants.FormField.NATIONALITY.name -> constants.FormField.NATIONALITY.field,
      constants.FormField.COUNTRY_OF_RESIDENCE.name -> constants.FormField.COUNTRY_OF_RESIDENCE.field,
      constants.FormField.RESIDENTIAL_ADDRESS.name -> constants.FormField.RESIDENTIAL_ADDRESS.field,
      constants.FormField.COUNTRY_CODE2.name -> constants.FormField.COUNTRY_CODE2.field,
      constants.FormField.POSTAL_ZIP_CODE.name -> constants.FormField.POSTAL_ZIP_CODE.field,
      constants.FormField.DATE_OF_BIRTH.name -> constants.FormField.DATE_OF_BIRTH.field,
      constants.FormField.IDENTIFICATION_TYPE.name -> constants.FormField.IDENTIFICATION_TYPE.field,
      constants.FormField.IDENTIFICATION_NUMBER.name -> constants.FormField.IDENTIFICATION_NUMBER.field,
      constants.FormField.ISSUE_DATE.name -> constants.FormField.ISSUE_DATE.field,
      constants.FormField.EXPIRY_DATE.name -> constants.FormField.EXPIRY_DATE.field
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
