package views.companion.master.memberCheck

import play.api.data.Form
import play.api.data.Forms.mapping

object MemberCheckVesselScan {
  val form = Form(
    mapping(
      constants.FormField.ASSET_ID.name -> constants.FormField.ASSET_ID.field,
      constants.FormField.VESSEL_NAME.name -> constants.FormField.VESSEL_NAME.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(assetID: String, vesselName: String)

}