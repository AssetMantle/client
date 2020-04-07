package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object AcceptOrRejectNegotiationTerms {
  val form = Form(
    mapping(
      constants.FormField.ID.name -> constants.FormField.ID.field,
      constants.FormField.TERM_TYPE.name -> constants.FormField.TERM_TYPE.field,
      constants.FormField.STATUS.name -> constants.FormField.STATUS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: String, termType: String, status: Boolean)
}
