package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.addOrganizationControllerTest.getOrganizationID
import controllersTest.addZoneControllerTest.{getZoneID, getZoneStatus}
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import controllersTest.issueAssetControllerTest.{allIssueAssetDocumentType, getAssetPriceForIssueAsset, getAssetQuantityForIssueAsset, getAssetTypeForIssueAsset, getDocumentHashForIssueAsset, getQuantityUnitForIssueAsset}
import controllersTest.issueFiatControllerTest.{getTransactionAmountForIssueFiat, getTransactionIDForIssueFiat}
//import controllersTest.changeBuyerBidControllerTest.getBuyerAddress
import controllersTest.issueAssetControllerTest.{getPegHashByOwnerAddress, getRequestIDForIssueAsset}
import controllersTest.issueFiatControllerTest.getRequestIDForIssueFiat
import controllersTest.sendCoinControllerTest.getRequestIDForFaucetRequest
import controllersTest.setACLControllerTest._
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._


class OneCompleteTransactionUnmoderated extends Simulation {

  val oneCompleteUnmoderated=scenario("ONE_COMPLETE_UNMODERATED")
    .exec(CreateZone.createZone)
    .exec(CreateOrganization.createOrganization)
    .exec(CreateSeller.createSeller)
    .exec(CreateBuyer.createBuyer)
    .exec(IssueAssetUnmoderated.issueAssetUnmoderated)
    .exec(IssueFiat.issueFiat)
    .exec(ChangeBuyerBid.changeBuyerBid)
    .exec(ChangeSellerBid.changeSellerBid)
    .exec(ConfirmBuyerBid.confirmBuyerBid)
    .exec(ConfirmSellerBid.confirmSellerBid)
    .exec(SendFiat.sendFiat)
    .exec(SendAsset.sendAsset)
    .exec(BuyerExecuteOrderUnmoderated.buyerExecuteOrderUnmoderated)
    .exec(SellerExecuteOrderUnmoderated.sellerExecuteOrderUnmoderated)
    .exec(SetBuyerFeedback.setBuyerFeedback)
    .exec(SetSellerFeedback.setSellerFeedback)
    .exec(RedeemAsset.redeemAsset)
    .exec(RedeemFiat.redeemFiat)

  setUp(
    oneCompleteUnmoderated.inject(atOnceUsers(1))
  )
    .maxDuration(1000)
    .protocols(http.baseUrl(Test.BASE_URL))
}

object IssueAssetUnmoderated{

  val issueAssetUnmoderated=scenario("IssueAssetUnmoderated")
    .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(issueAssetControllerTest.unmoderatedIssueAssetRequestScenario)
    .exec(logoutControllerTest.logoutScenario)
}

object BuyerExecuteOrderUnmoderated{

    val buyerExecuteOrderUnmoderated=scenario("BuyerExecuteOrderUnmoderated")
      .exec(session=> session.set(Test.TEST_NEGOTIATION_REQUEST_ID,confirmBuyerBidControllerTest.getNegotiationRequestIDFromSellerAccountID(session(Test.TEST_SELLER_USERNAME).as[String])))
      .exec(session=> session.set(Test.TEST_NEGOTIATION_ID,confirmBuyerBidControllerTest.getNegotiationIDFromSellerAccountID(session(Test.TEST_SELLER_USERNAME).as[String])))
      .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_BUYER_PASSWORD).as[String]))
      .exec(loginControllerTest.loginScenario)
      .exec(buyerExecuteOrderControllerTest.unmoderatedBuyerExecuteOrderScenario)
      .exec(logoutControllerTest.logoutScenario)
}

object SellerExecuteOrderUnmoderated{

  val sellerExecuteOrderUnmoderated=scenario("SellerExecuteOrderUnmoderated")
    .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(sellerExecuteOrderControllerTest.unmoderatedSellerExecuteOrder)
    .exec(logoutControllerTest.logoutScenario)
}
