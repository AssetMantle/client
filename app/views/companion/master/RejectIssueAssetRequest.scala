package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object RejectIssueAssetRequest {

  val form = Form(
    mapping(
      constants.Form.REQUEST_ID -> constants.FormField.REQUEST_ID.field,
      constants.Form.COMMENT -> constants.FormField.COMMENT.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, comment: String)

}
