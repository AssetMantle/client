package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

object RejectNegotiationRequest {

  val form = Form(
    mapping(
      constants.FormField.ID.name -> constants.FormField.ID.field,
      constants.FormField.COMMENT.name -> optional(constants.FormField.COMMENT.field),
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: String, comment: Option[String])

}
