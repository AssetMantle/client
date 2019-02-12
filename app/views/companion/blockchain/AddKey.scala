package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object AddKey {
  val form = Form(
    mapping(
      "name" -> nonEmptyText(),
      "password" -> nonEmptyText(),
      "seed" -> nonEmptyText()
    )(Data.apply)(Data.unapply)
  )

  case class Data(name: String, password: String, seed: String)

}
