package views.companion.master

import java.util.Date
import play.api.data.Form
import play.api.data.Forms.mapping

object Identification {
  val form = Form(
    mapping(
      constants.FormField.FIRST_NAME.name -> constants.FormField.FIRST_NAME.field,
      constants.FormField.LAST_NAME.name -> constants.FormField.LAST_NAME.field,
      constants.FormField.DATE_OF_BIRTH.name -> constants.FormField.DATE_OF_BIRTH.field,
      constants.FormField.ID_NUMBER.name -> constants.FormField.ID_NUMBER.field,
      constants.FormField.ID_TYPE.name -> constants.FormField.ID_TYPE.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(firstName: String, lastName:String, dateOfBirth: Date, idNumber: String, idType: String)

}
