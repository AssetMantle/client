package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object UserReviewIdentification {
  val form = Form(
    mapping(
      constants.FormField.COMPLETION.name -> constants.FormField.COMPLETION.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(completionStatus: Boolean)
}
