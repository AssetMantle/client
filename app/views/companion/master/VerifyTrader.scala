package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping}

object VerifyTrader {
  val form = Form(
    mapping(
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
      constants.FormField.ACL_ADDRESS.name -> constants.FormField.ACL_ADDRESS.field,
      constants.FormField.ORGANIZATION_ID.name -> constants.FormField.ORGANIZATION_ID.field,
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

    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, aclAddress: String, organizationID: String, issueAsset: Boolean, issueFiat: Boolean, sendAsset: Boolean, sendFiat: Boolean, redeemAsset: Boolean, redeemFiat: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAsset: Boolean)

}
