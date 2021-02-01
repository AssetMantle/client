package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object UpdatePrivatProperty {

  val form = Form(
    mapping(
      constants.FormField.ENTITY_ID.name -> constants.FormField.ENTITY_ID.field,
      constants.FormField.ENTITY_TYPE.name -> constants.FormField.ENTITY_TYPE.field,
      constants.FormField.NAME.name -> constants.FormField.NAME.field,
      constants.FormField.VALUE.name -> constants.FormField.VALUE.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(entityID: String, entityType: String, name: String, value: String)

}

