package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object RejectIssueFiatRequest {

  val form = Form(
    mapping(
      constants.FormField.REQUEST_ID.name -> constants.FormField.REQUEST_ID.field,
      constants.FormField.COMMENT.name -> constants.FormField.COMMENT.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, comment: String)

}
