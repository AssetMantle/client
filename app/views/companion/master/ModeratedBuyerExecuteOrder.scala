package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ModeratedBuyerExecuteOrder {

  val form = Form(
    mapping(
      constants.FormField.BUYER_ACCOUNT_ID.name -> constants.FormField.BUYER_ACCOUNT_ID.field,
      constants.FormField.SELLER_ACCOUNT_ID.name -> constants.FormField.SELLER_ACCOUNT_ID.field,
      constants.FormField.ASSET_ID.name -> constants.FormField.ASSET_ID.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(buyerAccountID: String, sellerAccountID: String, assetID: String, gas: Int, password: String)

}
