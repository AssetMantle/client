package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ConfirmSellerBid {
  val form = Form(
    mapping(
      constants.Form.PASSWORD -> nonEmptyText(minLength = constants.FormConstraint.PASSWORD_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH),
      constants.Form.BUYER_ADDRESS -> nonEmptyText(minLength = constants.FormConstraint.BLOCKCHAIN_ADDRESS_LENGTH, maxLength = constants.FormConstraint.BLOCKCHAIN_ADDRESS_LENGTH),
      constants.Form.BID -> number(min = constants.FormConstraint.BID_MINIMUM_VALUE, max = constants.FormConstraint.BID_MAXIMUM_VALUE),
      constants.Form.TIME -> number(min = constants.FormConstraint.BLOCK_TIME_MINIMUM_VALUE, max = constants.FormConstraint.BLOCK_TIME_MAXIMUM_VALUE),
      constants.Form.PEG_HASH -> nonEmptyText(minLength = constants.FormConstraint.PEG_HASH_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PEG_HASH_MAXIMUM_LENGTH),
      constants.Form.SELLER_CONTRACT_HASH -> nonEmptyText(minLength = 5, maxLength = 40),
      constants.Form.GAS -> number(min = constants.FormConstraint.GAS_MINIMUM_VALUE, max = constants.FormConstraint.GAS_MAXIMUM_VALUE)
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, buyerAddress: String, bid: Int, time: Int, pegHash: String, sellerContractHash: String, gas: Int)

}
