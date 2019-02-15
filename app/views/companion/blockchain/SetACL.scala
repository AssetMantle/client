package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

object SetACL {
  val form = Form(
    mapping(
      "from" -> nonEmptyText(minLength = 1, maxLength = 20),
      "password" -> nonEmptyText(minLength = 1, maxLength = 20),
      "aclAddress" -> nonEmptyText(minLength = 1, maxLength = 45),
      "organizationID" -> nonEmptyText(minLength = 1, maxLength = 20),
      "zoneID" -> nonEmptyText(minLength = 1, maxLength = 20),
      "chainID" -> nonEmptyText(minLength = 1, maxLength = 20),
      "issueAsset" -> nonEmptyText(minLength = 1, maxLength = 20),
      "issueFiat" -> nonEmptyText(minLength = 1, maxLength = 20),
      "sendAsset" -> nonEmptyText(minLength = 1, maxLength = 20),
      "sendFiat" -> nonEmptyText(minLength = 1, maxLength = 20),
      "redeemAssets" -> nonEmptyText(minLength = 1, maxLength = 20),
      "redeemFiats" -> nonEmptyText(minLength = 1, maxLength = 20),
      "sellerExecuteOrder" -> nonEmptyText(minLength = 1, maxLength = 20),
      "buyerExecuteOrder" -> nonEmptyText(minLength = 1, maxLength = 20),
      "changeBuyerBid" -> nonEmptyText(minLength = 1, maxLength = 20),
      "changeSellerBid" -> nonEmptyText(minLength = 1, maxLength = 20),
      "confirmBuyerBid" -> nonEmptyText(minLength = 1, maxLength = 20),
      "confirmSellerBid" -> nonEmptyText(minLength = 1, maxLength = 20),
      "negotiation" -> nonEmptyText(minLength = 1, maxLength = 20),
      "releaseAssets" -> nonEmptyText(minLength = 1, maxLength = 20)

    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, password: String, aclAddress: String, organizationID: String, zoneID: String, chainID: String, issueAsset: String, issueFiat: String, sendAsset: String, sendFiat: String, redeemAsset: String, redeemFiat: String, sellerExecuteOrder: String, buyerExecuteOrder: String, changeBuyerBid: String, changeSellerBid: String, confirmBuyerBid: String, confirmSellerBid: String, negotiation: String, releaseAssets: String)

}
