package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object RejectVerifyZoneRequest {

  val form = Form(
    mapping(
      constants.Form.ZONE_ID -> nonEmptyText(minLength = 4, maxLength = 45),
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String)

}