package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object AcceptOrRejectSalesQuote {

  val form = Form(
    mapping(
      constants.FormField.SALES_QUOTE_ID.name -> constants.FormField.SALES_QUOTE_ID.field,
      constants.FormField.STATUS.name -> constants.FormField.STATUS.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(salesQuoteID: String, status: Boolean)
}
