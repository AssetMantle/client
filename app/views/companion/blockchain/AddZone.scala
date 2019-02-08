package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object AddZone {
  val form = Form(
    mapping(
      "from" -> nonEmptyText,
      "to" -> nonEmptyText,
      "zoneID" -> nonEmptyText,
      "chainID" -> nonEmptyText,
      "password" -> nonEmptyText

    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, zoneID: String, chainID: String, password: String)

}
