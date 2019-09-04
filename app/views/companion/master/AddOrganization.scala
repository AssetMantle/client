package views.companion.master

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._


object AddOrganization {

  val form = Form(
    mapping(
      constants.FormField.ZONE_ID.name -> constants.FormField.ZONE_ID.field,
      constants.FormField.NAME.name -> constants.FormField.NAME.field,
      constants.FormField.ABBREVIATION.name -> constants.FormField.ABBREVIATION.field,
      constants.FormField.ESTABLISHMENT_DATE.name -> constants.FormField.ESTABLISHMENT_DATE.field,
      constants.FormField.REGISTERED_ADDRESS_LINE_1.name -> constants.FormField.REGISTERED_ADDRESS_LINE_1.field,
      constants.FormField.REGISTERED_ADDRESS_LINE_2.name -> constants.FormField.REGISTERED_ADDRESS_LINE_2.field,
      constants.FormField.REGISTERED_LANDMARK.name -> constants.FormField.REGISTERED_LANDMARK.field,
      constants.FormField.REGISTERED_CITY.name -> constants.FormField.REGISTERED_CITY.field,
      constants.FormField.REGISTERED_COUNTRY.name -> constants.FormField.REGISTERED_COUNTRY.field,
      constants.FormField.REGISTERED_ZIP_CODE.name -> constants.FormField.REGISTERED_ZIP_CODE.field,
      constants.FormField.REGISTERED_PHONE.name -> constants.FormField.REGISTERED_PHONE.field,
      constants.FormField.POSTAL_ADDRESS_LINE_1.name -> constants.FormField.POSTAL_ADDRESS_LINE_1.field,
      constants.FormField.POSTAL_ADDRESS_LINE_2.name -> constants.FormField.POSTAL_ADDRESS_LINE_2.field,
      constants.FormField.POSTAL_LANDMARK.name -> constants.FormField.POSTAL_LANDMARK.field,
      constants.FormField.POSTAL_CITY.name -> constants.FormField.POSTAL_CITY.field,
      constants.FormField.POSTAL_COUNTRY.name -> constants.FormField.POSTAL_COUNTRY.field,
      constants.FormField.POSTAL_ZIP_CODE.name -> constants.FormField.POSTAL_ZIP_CODE.field,
      constants.FormField.POSTAL_PHONE.name -> constants.FormField.POSTAL_PHONE.field,
      constants.FormField.EMAIL_ADDRESS.name -> constants.FormField.EMAIL_ADDRESS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, name: String, abbreviation: String, establishmentDate: Date, registeredAddressLine1: String, registeredAddressLine2: String, registeredAddressLandmark: String, registeredAddressCity: String, registeredAddressCountry: String, registeredAddressZipCode: String, registeredAddressPhone: String, postalAddressLine1: String, postalAddressLine2: String, postalAddressLandmark: String, postalAddressCity: String, postalAddressCountry: String, postalAddressZipCode: String, postalAddressPhone: String, email: String)

}
