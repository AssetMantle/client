package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, boolean}

object SetACL {
  val form = Form(
    mapping(
      constants.Form.FROM -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ACL_ADDRESS -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.ORGANIZATION_ID -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ZONE_ID -> nonEmptyText(minLength = 1, maxLength = 20),
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

  case class Data(from: String, password: String, aclAddress: String, organizationID: String, zoneID: String, issueAsset: Boolean, issueFiat: Boolean, sendAsset: Boolean, sendFiat: Boolean, redeemAsset: Boolean, redeemFiat: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAsset: Boolean)

}
