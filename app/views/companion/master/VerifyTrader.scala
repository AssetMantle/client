package views.companion.master

import play.api.data.Form
import play.api.data.Forms.{boolean, mapping, nonEmptyText}

object VerifyTrader {
  val form = Form(
    mapping(
      constants.Form.PASSWORD -> nonEmptyText(minLength = constants.FormConstraint.PASSWORD_MINIMUM_LENGTH, maxLength = constants.FormConstraint.PASSWORD_MAXIMUM_LENGTH),
      constants.Form.ACL_ADDRESS -> nonEmptyText(minLength = constants.FormConstraint.BLOCKCHAIN_ADDRESS_LENGTH, maxLength = constants.FormConstraint.BLOCKCHAIN_ADDRESS_LENGTH),
      constants.Form.ORGANIZATION_ID -> nonEmptyText(minLength = constants.FormConstraint.ORGANIZATION_ID_MINIMUM_LENGTH, maxLength = constants.FormConstraint.ORGANIZATION_ID_MAXIMUM_LENGTH),
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
