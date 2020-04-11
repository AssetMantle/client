package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

object RejectTraderRequest {

  val form = Form(
    mapping(
      constants.FormField.TRADER_ID.name -> constants.FormField.TRADER_ID.field,
      constants.FormField.COMMENT.name -> optional(constants.FormField.COMMENT.field),
    )(Data.apply)(Data.unapply)
  )

  case class Data(traderID: String, comment: Option[String] = None)

}
