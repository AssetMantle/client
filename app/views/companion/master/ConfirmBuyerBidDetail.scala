package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ConfirmBuyerBidDetail {
  val form = Form(
    mapping(
      constants.FormField.REQUEST_ID.name -> optional(constants.FormField.REQUEST_ID.field),
      constants.FormField.SELLER_ADDRESS.name -> constants.FormField.SELLER_ADDRESS.field,
      constants.FormField.BID.name -> constants.FormField.BID.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: Option[String], sellerAddress: String, bid: Int, pegHash: String)

}
//      constants.FormField.BUYER_CONTRACT_HASH.name -> constants.FormField.BUYER_CONTRACT_HASH.field,