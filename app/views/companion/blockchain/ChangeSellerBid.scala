package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms._

object ChangeSellerBid {
  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.BID.name -> constants.FormField.BID.field,
      constants.FormField.TIME.name -> constants.FormField.TIME.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
      constants.FormField.MODE.name -> constants.FormField.MODE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, to: String, bid: Int, time: Int, pegHash: String, mode: String)

}
