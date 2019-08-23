package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object RejectVerifyTraderRequest {

  val form = Form(
    mapping(
      constants.Form.TRADER_ID -> constants.FormField.TRADER_ID.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(traderID: String)

}
