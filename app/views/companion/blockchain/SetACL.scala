package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping}

object SetACL {
  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
      constants.FormField.ACL_ADDRESS.name -> constants.FormField.ACL_ADDRESS.field,
      constants.FormField.ORGANIZATION_ID.name -> constants.FormField.ORGANIZATION_ID.field,
      constants.FormField.ZONE_ID.name -> constants.FormField.ZONE_ID.field,
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
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, aclAddress: String, organizationID: String, zoneID: String, issueAsset: Boolean, issueFiat: Boolean, sendAsset: Boolean, sendFiat: Boolean, redeemAsset: Boolean, redeemFiat: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAsset: Boolean, gas: Int)

}
