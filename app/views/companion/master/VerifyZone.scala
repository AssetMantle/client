package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object VerifyZone {


  val form = Form(
    mapping(
      constants.Form.ZONE_ID -> nonEmptyText(minLength = 4, maxLength = 20),
      constants.Form.PASSWORD -> nonEmptyText(minLength = 4, maxLength = 20),
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, password: String)

}
