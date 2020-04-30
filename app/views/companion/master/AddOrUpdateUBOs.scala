package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, seq}

object AddOrUpdateUBOs {

  val form = Form(
    mapping(
      constants.FormField.UBOS.name -> seq(optional(mapping(
        constants.FormField.PERSON_FIRST_NAME.name -> constants.FormField.PERSON_FIRST_NAME.field,
        constants.FormField.PERSON_LAST_NAME.name -> constants.FormField.PERSON_LAST_NAME.field,
        constants.FormField.SHARE_PERCENTAGE.name -> constants.FormField.SHARE_PERCENTAGE.field,
        constants.FormField.RELATIONSHIP.name -> constants.FormField.RELATIONSHIP.field,
        constants.FormField.TITLE.name -> constants.FormField.TITLE.field
      )(UBOData.apply)(UBOData.unapply)))
    )(Data.apply)(Data.unapply)
  )

  case class UBOData(personFirstName: String, personLastName: String, sharePercentage: Double, relationship: String, title: String)

  case class Data(ubos: Seq[Option[UBOData]])

}