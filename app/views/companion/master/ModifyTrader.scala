package views.companion.master

import play.api.data.Form
import play.api.data.Forms.mapping
import utilities.MicroNumber

object ModifyTrader {
  val form = Form(
    mapping(
      constants.FormField.ACCOUNT_ID.name -> constants.FormField.ACCOUNT_ID.field,
      constants.FormField.ISSUE_ASSET.name -> constants.FormField.ISSUE_ASSET.field,
      constants.FormField.ISSUE_FIAT.name -> constants.FormField.ISSUE_FIAT.field,
      constants.FormField.SEND_ASSET.name -> constants.FormField.SEND_ASSET.field,
      constants.FormField.SEND_FIAT.name -> constants.FormField.SEND_FIAT.field,
      constants.FormField.REDEEM_ASSET.name -> constants.FormField.REDEEM_ASSET.field,
      constants.FormField.REDEEM_FIAT.name -> constants.FormField.REDEEM_FIAT.field,
      constants.FormField.SELLER_EXECUTE_ORDER.name -> constants.FormField.SELLER_EXECUTE_ORDER.field,
      constants.FormField.BUYER_EXECUTE_ORDER.name -> constants.FormField.BUYER_EXECUTE_ORDER.field,
      constants.FormField.CHANGE_BUYER_BID.name -> constants.FormField.CHANGE_BUYER_BID.field,
      constants.FormField.CHANGE_SELLER_BID.name -> constants.FormField.CHANGE_SELLER_BID.field,
      constants.FormField.CONFIRM_BUYER_BID.name -> constants.FormField.CONFIRM_BUYER_BID.field,
      constants.FormField.CONFIRM_SELLER_BID.name -> constants.FormField.CONFIRM_SELLER_BID.field,
      constants.FormField.NEGOTIATION.name -> constants.FormField.NEGOTIATION.field,
      constants.FormField.RELEASE_ASSET.name -> constants.FormField.RELEASE_ASSET.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(accountID: String, issueAsset: Boolean = false, issueFiat: Boolean = false, sendAsset: Boolean = false, sendFiat: Boolean = false, redeemAsset: Boolean = false, redeemFiat: Boolean = false, sellerExecuteOrder: Boolean = false, buyerExecuteOrder: Boolean = false, changeBuyerBid: Boolean = false, changeSellerBid: Boolean = false, confirmBuyerBid: Boolean = false, confirmSellerBid: Boolean = false, negotiation: Boolean = false, releaseAsset: Boolean = false, gas: MicroNumber, password: String)

}
