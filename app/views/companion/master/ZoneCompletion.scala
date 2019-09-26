package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{mapping, boolean}

object ZoneCompletion {
  val form = Form(
    mapping(
      constants.Form.COMPLETION -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(completion: Boolean)

}
