package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping}

object OrganizationAgreement {
  val form = Form(
    mapping(
      constants.Form.AGREEMENT -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(agreement: Boolean)

}
