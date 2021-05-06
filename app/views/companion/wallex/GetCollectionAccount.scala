package views.companion.wallex

import play.api.data.Form
import play.api.data.Forms.mapping

object GetCollectionAccount {

  val form = Form(
    mapping(
      constants.FormField.ACCOUNT_ID.name -> constants.FormField.ACCOUNT_ID.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(
      accountID: String
  )

}
