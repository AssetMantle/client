package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

import java.util.Date

object ServiceCheckRequest {

  val form = Form(
    mapping(
      constants.FormField.CHECK_TYPE.name -> constants.FormField.CHECK_TYPE.field,
      constants.FormField.VESSEL_NAME.name -> constants.FormField.VESSEL_NAME.field,
      constants.FormField.SANCTIONED_COUNTRIES.name -> constants.FormField.SANCTIONED_COUNTRIES.field,
      constants.FormField.REFERENCE_ID.name -> constants.FormField.REFERENCE_ID.field,
      constants.FormField.VESSEL_CHECK_FROM_DATE.name -> constants.FormField.VESSEL_CHECK_FROM_DATE.field,
      constants.FormField.VESSEL_CHECK_TO_DATE.name -> constants.FormField.VESSEL_CHECK_TO_DATE.field,
      constants.FormField.ORGANIZATION_NAME.name -> constants.FormField.ORGANIZATION_NAME.field,
      constants.FormField.COUNTRY.name -> constants.FormField.COUNTRY.field,
      constants.FormField.ORGANIZATION_TYPE.name -> constants.FormField.ORGANIZATION_TYPE.field,

    )(Data.apply)(Data.unapply)
  )

  case class Data(
      checkType: String,
      vesselName: String,
      sanctionedCountries : String,
      referenceId: String,
      fromDate: Date,
      toDate: Date,
      organizationName : String,
      country : String,
      organizationType : String
  )
}
