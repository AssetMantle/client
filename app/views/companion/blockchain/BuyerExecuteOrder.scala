package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object BuyerExecuteOrder {
  val form = Form(
    mapping(
      constants.Forms.FROM -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.BUYER_ADDRESS -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Forms.SELLER_ADDRESS -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Forms.FIAT_PROOF_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.PEG_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.GAS -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int)

}
