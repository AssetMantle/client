package views.companion.master

import play.api.data.Form
import play.api.data.Forms._


object AddTrader {

  val form = Form(
    mapping(
      constants.Form.ZONE_ID -> nonEmptyText(minLength = 8, maxLength = 50),
      constants.Form.ORGANIZATION_ID -> nonEmptyText(minLength = 8, maxLength = 50),
      constants.Form.NAME -> nonEmptyText(minLength = 8, maxLength = 50),
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, organizationID: String, name: String)

}
