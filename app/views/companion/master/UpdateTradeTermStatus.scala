package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object UpdateTradeTermStatus {
  val form = Form(
    mapping(
      constants.FormField.TRADE_ID.name -> constants.FormField.TRADE_ID.field,
      constants.FormField.TERM_TYPE.name -> constants.FormField.TERM_TYPE.field,
      constants.FormField.STATUS.name -> constants.FormField.STATUS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(tradeID: String, termType: String, status: Boolean)
}