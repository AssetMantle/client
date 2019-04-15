package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object AddZone {
  val form = Form(
    mapping(
      constants.Form.FROM -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.TO -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.ZONE_ID -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20)

    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, zoneID: String, password: String)

}
