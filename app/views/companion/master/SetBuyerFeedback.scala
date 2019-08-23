package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SetBuyerFeedback {
  val form = Form(
    mapping(
      constants.Form.PASSWORD -> constants.FormField.PASSWORD.field,
      constants.Form.SELLER_ADDRESS -> constants.FormField.BLOCKCHAIN_ADDRESS.field,
      constants.Form.PEG_HASH -> constants.FormField.PEG_HASH.field,
      constants.Form.RATING -> constants.FormField.RATING.field,
      constants.Form.GAS -> constants.FormField.GAS.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, sellerAddress: String, pegHash: String, rating: Int, gas: Int)

}