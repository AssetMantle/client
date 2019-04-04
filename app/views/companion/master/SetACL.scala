package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping, nonEmptyText}

object SetACL {
  val form = Form(
    mapping(
      constants.Forms.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.ACL_ADDRESS -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Forms.ORGANIZATION_ID -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.ZONE_ID -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.ISSUE_ASSET -> boolean,
      constants.Forms.ISSUE_FIAT -> boolean,
      constants.Forms.SEND_ASSET -> boolean,
      constants.Forms.SEND_FIAT -> boolean,
      constants.Forms.REDEEM_ASSET -> boolean,
      constants.Forms.REDEEM_FIAT -> boolean,
      constants.Forms.SELLER_EXECUTE_ORDER -> boolean,
      constants.Forms.BUYER_EXECUTE_ORDER -> boolean,
      constants.Forms.CHANGE_BUYER_BID -> boolean,
      constants.Forms.CHANGE_SELLER_BID -> boolean,
      constants.Forms.CONFIRM_BUYER_BID -> boolean,
      constants.Forms.CONFIRM_SELLER_BID -> boolean,
      constants.Forms.NEGOTIATION -> boolean,
      constants.Forms.RELEASE_ASSET -> boolean,

    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String, aclAddress: String, organizationID: String, zoneID: String, issueAsset: Boolean, issueFiat: Boolean, sendAsset: Boolean, sendFiat: Boolean, redeemAsset: Boolean, redeemFiat: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAsset: Boolean)

}
