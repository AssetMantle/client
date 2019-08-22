package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object RejectVerifyZoneRequest {

  val form = Form(
    mapping(
      constants.Form.ZONE_ID -> nonEmptyText(minLength = constants.FormConstraint.ZONE_ID_MINIMUM_LENGTH, maxLength = constants.FormConstraint.ZONE_ID_MAXIMUM_LENGTH),
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String)

}
