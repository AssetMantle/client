package views.companion.master

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._


object AddOrganization {

  val form = Form(
    mapping(
      constants.FormField.ZONE_ID.name -> constants.FormField.ZONE_ID.field,
      constants.FormField.NAME.name -> constants.FormField.NAME.field,
      constants.FormField.ABBREVIATION.name -> optional(constants.FormField.ABBREVIATION.field),
      constants.FormField.ESTABLISHMENT_DATE.name -> constants.FormField.ESTABLISHMENT_DATE.field,
      constants.FormField.EMAIL_ADDRESS.name -> constants.FormField.EMAIL_ADDRESS.field,
      constants.Form.REGISTERED_ADDRESS -> mapping(
        constants.FormField.ADDRESS_LINE_1.name -> constants.FormField.ADDRESS_LINE_1.field,
        constants.FormField.ADDRESS_LINE_2.name -> constants.FormField.ADDRESS_LINE_2.field,
        constants.FormField.LANDMARK.name -> optional(constants.FormField.LANDMARK.field),
        constants.FormField.CITY.name -> constants.FormField.CITY.field,
        constants.FormField.COUNTRY.name -> constants.FormField.COUNTRY.field,
        constants.FormField.ZIP_CODE.name -> constants.FormField.ZIP_CODE.field,
        constants.FormField.PHONE.name -> constants.FormField.PHONE.field,
      )(AddressData.apply)(AddressData.unapply),
      constants.Form.POSTAL_ADDRESS -> mapping(
        constants.FormField.ADDRESS_LINE_1.name -> constants.FormField.ADDRESS_LINE_1.field,
        constants.FormField.ADDRESS_LINE_2.name -> constants.FormField.ADDRESS_LINE_2.field,
        constants.FormField.LANDMARK.name -> optional(constants.FormField.LANDMARK.field),
        constants.FormField.CITY.name -> constants.FormField.CITY.field,
        constants.FormField.COUNTRY.name -> constants.FormField.COUNTRY.field,
        constants.FormField.ZIP_CODE.name -> constants.FormField.ZIP_CODE.field,
        constants.FormField.PHONE.name -> constants.FormField.PHONE.field
      )(AddressData.apply)(AddressData.unapply)
    )(Data.apply)(Data.unapply)
  )

  case class AddressData(addressLine1: String, addressLine2: String, Landmark: Option[String], City: String, Country: String, ZipCode: String, Phone: String)

  case class Data(zoneID: String, name: String, abbreviation: Option[String], establishmentDate: Date, email: String, registeredAddress: AddressData, postalAddress: AddressData)

}
