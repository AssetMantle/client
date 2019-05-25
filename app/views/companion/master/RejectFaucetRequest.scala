package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object RejectFaucetRequest {

  val form = Form(
    mapping(
      constants.Form.REQUEST_ID -> nonEmptyText(minLength = 4, maxLength = 45),
      constants.Form.COMMENT -> nonEmptyText(minLength = 0, maxLength = 100),
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, comment: String)

}
