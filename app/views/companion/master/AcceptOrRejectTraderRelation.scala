package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object AcceptOrRejectTraderRelation {

  val form = Form(
    mapping(
      constants.FormField.FROM_ID.name -> constants.FormField.FROM_ID.field,
      constants.FormField.TO_ID.name -> constants.FormField.TO_ID.field,
      constants.FormField.STATUS.name -> constants.FormField.STATUS.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(fromID: String, toID: String, status: Boolean)
}
