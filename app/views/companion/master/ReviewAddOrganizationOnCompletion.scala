package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping}

object ReviewAddOrganizationOnCompletion {
  val form = Form(
    mapping(
      constants.Form.COMPLETION -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(completion: Boolean)

}
