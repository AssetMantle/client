package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object RedeemAsset {
  val form = Form(
    mapping(
      constants.Form.ZONE_ID -> nonEmptyText(minLength = constants.FormConstraint.ZONE_ID_MINIMUM_LENGTH, maxLength = constants.FormConstraint.ZONE_ID_MAXIMUM_LENGTH),
      constants.Form.PEG_HASH -> nonEmptyText(minLength = constants.FormConstraint.PEG_HASH_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PEG_HASH_MAXIMUM_LENGTH),
      constants.Form.PASSWORD -> nonEmptyText(minLength = constants.FormConstraint.PASSWORD_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH),
      constants.Form.GAS -> number(min = constants.FormConstraint.GAS_MINIMUM_VALUE, max = constants.FormConstraint.GAS_MAXIMUM_VALUE)
    )(Data.apply)(Data.unapply)
  )

  case class Data(zoneID: String, pegHash: String, password: String, gas: Int)

}