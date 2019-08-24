package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SellerExecuteOrder {
  val form = Form(
    mapping(
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field,
      constants.FormField.BUYER_ADDRESS.name -> constants.FormField.BUYER_ADDRESS.field,
      constants.FormField.AWB_PROOF_HASH.name -> constants.FormField.AWB_PROOF_HASH.field,
      constants.FormField.PEG_HASH.name -> constants.FormField.PEG_HASH.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, buyerAddress: String, awbProofHash: String, pegHash: String)


}
