package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object RejectVerifyTraderRequest {

  val form = Form(
    mapping(
      constants.FormField.TRADER_ID.name -> constants.FormField.TRADER_ID.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(traderID: String)

}
