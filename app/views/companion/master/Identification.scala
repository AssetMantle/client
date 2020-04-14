package views.companion.master

import java.util.Date

import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

object Identification {
  val form = Form(
    mapping(
      constants.FormField.FIRST_NAME.name -> constants.FormField.FIRST_NAME.field,
      constants.FormField.LAST_NAME.name -> constants.FormField.LAST_NAME.field,
      constants.FormField.DATE_OF_BIRTH.name -> constants.FormField.DATE_OF_BIRTH.field,
      constants.FormField.ID_NUMBER.name -> constants.FormField.ID_NUMBER.field,
      constants.FormField.ID_TYPE.name -> constants.FormField.ID_TYPE.field,
      constants.FormField.ADDRESS.name -> mapping(
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

  case class AddressData(addressLine1: String, addressLine2: String, landmark: Option[String], city: String, country: String, zipCode: String, phone: String)

  case class Data(firstName: String, lastName:String, dateOfBirth: Date, idNumber: String, idType: String, address: AddressData)

}