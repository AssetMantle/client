package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object  ChangeBuyerBid {
  val form = Form(
    mapping(
      constants.FormField.REQUEST_ID.name -> optional(constants.FormField.REQUEST_ID.field),
      constants.FormField.SELLER_ADDRESS.name -> constants.FormField.SELLER_ADDRESS.field,
      constants.FormField.BID.name -> constants.FormField.BID.field,
      constants.FormField.TIME.name -> constants.FormField.TIME.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: Option[String], sellerAddress: String, bid: Int, time: Int, pegHash: String, gas: Int, password: String)

}
