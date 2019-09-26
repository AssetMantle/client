package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping}

object ModifyTrader {
  val form = Form(
    mapping(
      constants.FormField.ACCOUNT_ID.name -> constants.FormField.ACCOUNT_ID.field,
      constants.Form.ISSUE_ASSET -> boolean,
      constants.Form.ISSUE_FIAT -> boolean,
      constants.Form.SEND_ASSET -> boolean,
      constants.Form.SEND_FIAT -> boolean,
      constants.Form.REDEEM_ASSET -> boolean,
      constants.Form.REDEEM_FIAT -> boolean,
      constants.Form.SELLER_EXECUTE_ORDER -> boolean,
      constants.Form.BUYER_EXECUTE_ORDER -> boolean,
      constants.Form.CHANGE_BUYER_BID -> boolean,
      constants.Form.CHANGE_SELLER_BID -> boolean,
      constants.Form.CONFIRM_BUYER_BID -> boolean,
      constants.Form.CONFIRM_SELLER_BID -> boolean,
      constants.Form.NEGOTIATION -> boolean,
      constants.Form.RELEASE_ASSET -> boolean,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(accountID: String, issueAsset: Boolean = false, issueFiat: Boolean = false, sendAsset: Boolean = false, sendFiat: Boolean = false, redeemAsset: Boolean = false, redeemFiat: Boolean = false, sellerExecuteOrder: Boolean = false, buyerExecuteOrder: Boolean = false, changeBuyerBid: Boolean = false, changeSellerBid: Boolean = false, confirmBuyerBid: Boolean = false, confirmSellerBid: Boolean = false, negotiation: Boolean = false, releaseAsset: Boolean = false, gas: Int, password: String)

}
