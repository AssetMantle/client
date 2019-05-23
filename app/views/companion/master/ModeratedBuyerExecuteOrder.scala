package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ModeratedBuyerExecuteOrder {
  val form = Form(
    mapping(
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.SELLER_ADDRESS -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.FIAT_PROOF_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.PEG_HASH -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.GAS -> number(min = 1, max = 10000)
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int)

}
