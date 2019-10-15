package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.mapping

object SetACL {
  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
      constants.FormField.ACL_ADDRESS.name -> constants.FormField.ACL_ADDRESS.field,
      constants.FormField.ORGANIZATION_ID.name -> constants.FormField.ORGANIZATION_ID.field,
      constants.FormField.ZONE_ID.name -> constants.FormField.ZONE_ID.field,
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
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, aclAddress: String, organizationID: String, zoneID: String, issueAsset: Boolean, issueFiat: Boolean, sendAsset: Boolean, sendFiat: Boolean, redeemAsset: Boolean, redeemFiat: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAsset: Boolean, gas: Int)

}
