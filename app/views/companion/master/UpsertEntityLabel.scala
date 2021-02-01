package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object UpsertEntityLabel {

  val form = Form(
    mapping(
      constants.FormField.ENTITY_ID.name -> constants.FormField.ENTITY_ID.field,
      constants.FormField.ENTITY_TYPE.name -> constants.FormField.ENTITY_TYPE.field,
      constants.FormField.LABEL.name -> constants.FormField.LABEL.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(entityID: String, entityType: String, label: String)

}

