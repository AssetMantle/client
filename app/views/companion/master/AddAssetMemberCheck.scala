package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object AddAssetMemberCheck {

  val form = Form(
    mapping(
      constants.FormField.ASSET_ID.name -> constants.FormField.ASSET_ID.field,
      constants.FormField.SCAN_ID.name -> constants.FormField.SCAN_ID.field,
      constants.FormField.RESULT_ID.name -> optional(constants.FormField.RESULT_ID.field),
      constants.FormField.STATUS.name -> constants.FormField.STATUS.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(assetID: String, scanID: Int, resultID: Option[Int], status: Boolean)

}

