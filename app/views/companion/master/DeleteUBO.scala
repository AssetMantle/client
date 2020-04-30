package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, seq}

object DeleteUBO {

  val form = Form(
    mapping(
      constants.FormField.PERSON_FIRST_NAME.name -> constants.FormField.PERSON_FIRST_NAME.field,
      constants.FormField.PERSON_LAST_NAME.name -> constants.FormField.PERSON_LAST_NAME.field,
      constants.FormField.SHARE_PERCENTAGE.name -> constants.FormField.SHARE_PERCENTAGE.field,
      constants.FormField.RELATIONSHIP.name -> constants.FormField.RELATIONSHIP.field,
      constants.FormField.TITLE.name -> constants.FormField.TITLE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(personFirstName: String, personLastName: String, sharePercentage: Double, relationship: String, title: String)

}