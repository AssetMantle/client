package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object AddZone {
  val form = Form(
    mapping(
      constants.Form.NAME -> nonEmptyText(minLength = constants.FormConstraint.NAME_MINIMUM_LENGTH, maxLength = constants.FormConstraint.NAME_MAXIMUM_LENGTH),
      constants.Form.CURRENCY -> nonEmptyText(minLength = constants.FormConstraint.CURRENCY_MINIMUM_LENGTH, maxLength = constants.FormConstraint.CURRENCY_MAXIMUM_LENGTH)

    )(Data.apply)(Data.unapply)
  )

  case class Data(name: String, currency: String)

}
