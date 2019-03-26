package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, boolean}

object SetACL {
  val form = Form(
    mapping(
      "from" -> nonEmptyText(minLength = 1, maxLength = 20),
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "aclAddress" -> nonEmptyText(minLength = 1, maxLength = 45),
      "organizationID" -> nonEmptyText(minLength = 1, maxLength = 20),
      "zoneID" -> nonEmptyText(minLength = 1, maxLength = 20),
      "issueAsset" -> boolean,
      "issueFiat" -> boolean,
      "sendAsset" -> boolean,
      "sendFiat" -> boolean,
      "redeemAsset" -> boolean,
      "redeemFiat" -> boolean,
      "sellerExecuteOrder" -> boolean,
      "buyerExecuteOrder" -> boolean,
      "changeBuyerBid" -> boolean,
      "changeSellerBid" -> boolean,
      "confirmBuyerBid" -> boolean,
      "confirmSellerBid" -> boolean,
      "negotiation" -> boolean,
      "releaseAsset" -> boolean,

    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, aclAddress: String, organizationID: String, zoneID: String, issueAsset: Boolean, issueFiat: Boolean, sendAsset: Boolean, sendFiat: Boolean, redeemAsset: Boolean, redeemFiat: Boolean, sellerExecuteOrder: Boolean, buyerExecuteOrder: Boolean, changeBuyerBid: Boolean, changeSellerBid: Boolean, confirmBuyerBid: Boolean, confirmSellerBid: Boolean, negotiation: Boolean, releaseAsset: Boolean)

}
