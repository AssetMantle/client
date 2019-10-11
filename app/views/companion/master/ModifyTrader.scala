package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping}

object ModifyTrader {
  val form = Form(
    mapping(
      constants.FormField.ACCOUNT_ID.name -> constants.FormField.ACCOUNT_ID.field,
      constants.FormField.ISSUE_ASSET.name -> boolean,
      constants.FormField.ISSUE_FIAT.name -> boolean,
      constants.FormField.SEND_ASSET.name -> boolean,
      constants.FormField.SEND_FIAT.name -> boolean,
      constants.FormField.REDEEM_ASSET.name -> boolean,
      constants.FormField.REDEEM_FIAT.name -> boolean,
      constants.FormField.SELLER_EXECUTE_ORDER.name -> boolean,
      constants.FormField.BUYER_EXECUTE_ORDER.name -> boolean,
      constants.FormField.CHANGE_BUYER_BID.name -> boolean,
      constants.FormField.CHANGE_SELLER_BID.name -> boolean,
      constants.FormField.CONFIRM_BUYER_BID.name -> boolean,
      constants.FormField.CONFIRM_SELLER_BID.name -> boolean,
      constants.FormField.NEGOTIATION.name -> boolean,
      constants.FormField.RELEASE_ASSET.name -> boolean,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(accountID: String, issueAsset: Boolean = false, issueFiat: Boolean = false, sendAsset: Boolean = false, sendFiat: Boolean = false, redeemAsset: Boolean = false, redeemFiat: Boolean = false, sellerExecuteOrder: Boolean = false, buyerExecuteOrder: Boolean = false, changeBuyerBid: Boolean = false, changeSellerBid: Boolean = false, confirmBuyerBid: Boolean = false, confirmSellerBid: Boolean = false, negotiation: Boolean = false, releaseAsset: Boolean = false, gas: Int, password: String)

}
