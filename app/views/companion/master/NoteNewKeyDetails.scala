package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object NoteNewKeyDetails {
  val form = Form(
    mapping(
      constants.Form.CONFIRM_NOTE_NEW_KEY_DETAILS -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(confirmNoteNewKeyDetails: Boolean)

}
