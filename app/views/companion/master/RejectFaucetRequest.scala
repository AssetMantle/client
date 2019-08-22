package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object RejectFaucetRequest {

  val form = Form(
    mapping(
      constants.Form.REQUEST_ID -> nonEmptyText(minLength = constants.FormConstraint.REQUEST_ID_LENGTH, maxLength = constants.FormConstraint.REQUEST_ID_LENGTH),
      constants.Form.COMMENT -> nonEmptyText(minLength = constants.FormConstraint.COMMENT_MINIMUM_LENGTH, maxLength = constants.FormConstraint.COMMENT_MAXIMUM_LENGTH),
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, comment: String)

}
