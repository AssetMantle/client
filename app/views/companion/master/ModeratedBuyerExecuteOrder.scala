package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ModeratedBuyerExecuteOrder {

  val form = Form(
    mapping(
      constants.Form.PASSWORD -> nonEmptyText(minLength = constants.FormConstraint.PASSWORD_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH),
      constants.Form.BUYER_ADDRESS -> nonEmptyText(minLength = constants.FormConstraint.BLOCKCHAIN_ADDRESS_LENGTH, maxLength = constants.FormConstraint.BLOCKCHAIN_ADDRESS_LENGTH),
      constants.Form.SELLER_ADDRESS -> nonEmptyText(minLength = constants.FormConstraint.BLOCKCHAIN_ADDRESS_LENGTH, maxLength = constants.FormConstraint.BLOCKCHAIN_ADDRESS_LENGTH),
      constants.Form.FIAT_PROOF_HASH -> nonEmptyText(minLength = constants.FormConstraint.HASH_MINIMUM_LENGTH, maxLength = constants.FormConstraint.HASH_MAXIMUM_LENGTH),
      constants.Form.PEG_HASH -> nonEmptyText(minLength = constants.FormConstraint.PEG_HASH_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PEG_HASH_MAXIMUM_LENGTH),
      constants.Form.GAS -> number(min = constants.FormConstraint.GAS_MINIMUM_VALUE, max = constants.FormConstraint.GAS_MAXIMUM_VALUE)
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int)

}
