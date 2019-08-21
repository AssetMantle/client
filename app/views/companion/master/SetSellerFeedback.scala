package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SetSellerFeedback {
  val form = Form(
    mapping(
      constants.Form.PASSWORD -> nonEmptyText(minLength = constants.FormConstraint.PASSWORD_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH),
      constants.Form.BUYER_ADDRESS -> nonEmptyText(minLength = constants.FormConstraint.BLOCKCHAIN_ADDRESS_LENGTH, maxLength = constants.FormConstraint.BLOCKCHAIN_ADDRESS_LENGTH),
      constants.Form.PEG_HASH -> nonEmptyText(minLength = constants.FormConstraint.PEG_HASH_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PEG_HASH_MAXIMUM_LENGTH),
      constants.Form.RATING -> number(min = constants.FormConstraint.RATING_MINIMUM_VALUE, max = constants.FormConstraint.RATING_MAXIMUM_VALUE),
      constants.Form.GAS -> number(min = constants.FormConstraint.GAS_MINIMUM_VALUE, max = constants.FormConstraint.GAS_MAXIMUM_VALUE)
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, buyerAddress: String, pegHash: String, rating: Int, gas: Int)

}