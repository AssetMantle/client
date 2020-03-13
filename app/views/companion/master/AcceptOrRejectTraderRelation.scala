package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object AcceptOrRejectTraderRelation {

  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.STATUS.name -> constants.FormField.STATUS.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, status: Boolean)
}
