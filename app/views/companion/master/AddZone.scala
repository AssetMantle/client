package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object AddZone {
  val form = Form(
    mapping(
      constants.FormField.NAME.name -> constants.FormField.NAME.field,
      constants.FormField.CURRENCY.name -> constants.FormField.CURRENCY.field,
      constants.FormField.ADDRESS_LINE_1.name -> constants.FormField.ADDRESS_LINE_1.field,
      constants.FormField.ADDRESS_LINE_2.name -> constants.FormField.ADDRESS_LINE_2.field,
      constants.FormField.LANDMARK.name -> optional(constants.FormField.LANDMARK.field),
      constants.FormField.CITY.name -> constants.FormField.CITY.field,
      constants.FormField.COUNTRY.name -> constants.FormField.COUNTRY.field,
      constants.FormField.ZIP_CODE.name -> constants.FormField.ZIP_CODE.field,
      constants.FormField.PHONE.name -> constants.FormField.PHONE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(name: String, currency: String, addressLine1: String, addressLine2: String, landmark: Option[String], city: String, country: String, zipCode: String, phone: String)

}
