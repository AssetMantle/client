package simulations

import constants.Test
import scenarios._
import feeders.JDBCFeeder._
import feeders._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class InterZoneTrade extends Simulation {

  val twoZoneTradeScenario = scenario("InterZoneTrade")
    .exec(CreateZone.createZone)
    .exec(CreateSellerOrganization.createSellerOrganization)
    .exec(session => session.set(Test.TEST_SELL_ZONE_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_SELL_ZONE_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(CreateZone.createZone)
    .exec(CreateBuyerOrganization.createBuyerOrganization)
    .exec(session => session.set(Test.TEST_BUY_ZONE_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_BUY_ZONE_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(CreateSeller.createSeller)
    .exec(CreateBuyer.createBuyer)
    .exec(AddCounterParty.addCounterParty)
    .exec(IssueFiat.issueFiat)
    .exec(IssueAssetModerated.issueAssetModerated)
    .exec(CreateSalesQuote.createSalesQuote)
    .exec(AcceptSalesQuoteAndAllTradeTerms.acceptSalesQuoteAndAllTradeTerms)
    .exec(UploadContractAndOtherTradeDocuments.uploadContractAndOtherTradeDocuments)
    .exec(AcceptBillOfLading.acceptBillOfLading)
    .exec(session => session.set(Test.TEST_ZONE_USERNAME, session(Test.TEST_SELL_ZONE_USERNAME).as[String]).set(Test.TEST_ZONE_PASSWORD, session(Test.TEST_SELL_ZONE_PASSWORD).as[String]))
    .exec(VesselCheckAndReleaseAsset.vesselCheckAndReleaseAsset)
    .exec(BuyerConfirmNegotiation.buyerConfirmNegotiation)
    .exec(SellerConfirmNegotiation.sellerConfirmNegotiation)
    .exec(SendFiat.sendFiat)
    .exec(SendAsset.sendAsset)
    .exec(session => session.set(Test.TEST_ZONE_USERNAME, session(Test.TEST_SELL_ZONE_USERNAME).as[String]).set(Test.TEST_ZONE_PASSWORD, session(Test.TEST_SELL_ZONE_PASSWORD).as[String]))
    .exec(ModeratedSellerExecuteOrder.moderatedSellerExecuteOrder)
    .exec(session => session.set(Test.TEST_ZONE_USERNAME, session(Test.TEST_BUY_ZONE_USERNAME).as[String]).set(Test.TEST_ZONE_PASSWORD, session(Test.TEST_BUY_ZONE_PASSWORD).as[String]))
    .exec(ModeratedBuyerExecuteOrder.moderatedBuyerExecuteOrder)
    .exec(RedeemAsset.redeemAsset)
    .exec(RedeemFiat.redeemFiat)

  setUp(
    twoZoneTradeScenario.inject(atOnceUsers(1))
  ).protocols(http.baseUrl(Test.BASE_URL))
}

object ModeratedBuyerExecuteOrder {

  val moderatedBuyerExecuteOrder = scenario("ModeratedBuyerExecuteOrder")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(OrderControllerTest.moderatedBuyerExecuteOrderScenario)
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)

}

object ModeratedSellerExecuteOrder {

  val moderatedSellerExecuteOrder = scenario("ModeratedSellerExecuteOrder")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(OrderControllerTest.moderatedSellerExecuteOrderScenario)
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)

}