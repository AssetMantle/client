package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object SellerExecuteOrder {
  val form = Form(
    mapping(
      constants.Form.FROM -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.BUYER_ADDRESS -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.SELLER_ADDRESS -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.AWB_PROOF_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.PEG_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.GAS -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, buyerAddress: String, sellerAddress: String, awbProofHash: String, pegHash: String, gas: Int)

}
