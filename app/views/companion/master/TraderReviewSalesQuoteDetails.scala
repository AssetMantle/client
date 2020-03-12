package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object TraderReviewSalesQuoteDetails {
  val form = Form(
    mapping(
      constants.FormField.REQUEST_ID.name -> constants.FormField.REQUEST_ID.field,
      constants.FormField.COMPLETION.name -> constants.FormField.COMPLETION.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, completion: Boolean)

}
