package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object AddKey {
  val form = Form(
    mapping(
      constants.Forms.NAME -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.SEED -> nonEmptyText(minLength = 1, maxLength = 200)
    )(Data.apply)(Data.unapply)
  )

  case class Data(name: String, password: String, seed: String)

}
