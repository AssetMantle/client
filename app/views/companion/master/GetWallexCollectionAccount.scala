package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object GetWallexCollectionAccount {

  val form = Form(
    mapping(
      constants.FormField.ACCOUNT_ID.name -> constants.FormField.ACCOUNT_ID.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(
      accountId: String,
  )

}
