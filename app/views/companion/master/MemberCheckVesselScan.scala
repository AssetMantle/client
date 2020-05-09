package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping

object MemberCheckVesselScan {
  val form = Form(
    mapping(
      constants.FormField.VESSEL_NAME.name -> constants.FormField.VESSEL_NAME.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(vesselName: String)

}
