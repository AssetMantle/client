package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

object TraderRelationRequest {
  val form = Form(
    mapping(
      constants.FormField.TO.name -> constants.FormField.TO.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(to: String)

}
