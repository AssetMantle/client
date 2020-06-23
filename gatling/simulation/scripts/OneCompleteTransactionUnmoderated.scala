package scripts

import constants.Test
import controllersTest._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class OneCompleteTransactionUnmoderated extends Simulation {

  val oneCompleteModeratedScenario = scenario("OneCompleteTest")
    .exec(CreateZone.createZone)
    .exec(CreateSellerOrganization.createSellerOrganization)
    .exec(CreateBuyerOrganization.createBuyerOrganization)
    .exec(CreateSeller.createSeller)
    .exec(CreateBuyer.createBuyer)
    .exec(AddCounterParty.addCounterParty)
    .exec(IssueFiat.issueFiat)
    .exec(IssueAssetUnmoderated.issueAssetUnmoderated)
    .exec(CreateSalesQuote.createSalesQuote)
    .exec(AcceptSalesQuoteAndAllTradeTerms.acceptSalesQuoteAndAllTradeTerms)
    .exec(UploadContractAndOtherTradeDocuments.uploadContractAndOtherTradeDocuments)
    .exec(AcceptBillOfLading.acceptBillOfLading)
    .exec(BuyerConfirmNegotiation.buyerConfirmNegotiation)
    .exec(SellerConfirmNegotiation.sellerConfirmNegotiation)
    .exec(SendAsset.sendAsset)
    .exec(UnmoderatedBuyerExecuteOrder.unmoderatedBuyerExecuteOrder)
    .exec(UnmoderatedSellerExecuteOrder.unmoderatedSellerExecuteOrder)
    .exec(RedeemAsset.redeemAsset)


  setUp(
    oneCompleteModeratedScenario.inject(atOnceUsers(1))
  ).protocols(http.baseUrl(Test.BASE_URL))
}

object IssueAssetUnmoderated {

  val issueAssetUnmoderated = scenario("IssueAssetModerated")
    //.feed(TempFeeder.timeFeed)
    /*.exec(session => session.set(Test.TEST_SELLER_USERNAME, "SELL10Ucz5z4Mh").set(Test.TEST_SELLER_PASSWORD, "SELL10Ucz5z4Mh"))
    .exec(session => session.set(Test.TEST_BUYER_USERNAME, "BUY109CP1qfJE").set(Test.TEST_BUYER_PASSWORD, "BUY109CP1qfJE"))*/
    .exec { session => session.set(Test.TEST_SELLER_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_BUYER_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(assetControllerTest.unmoderatedIssueAssetRequestScenario)
    .exec(accountControllerTest.logoutScenario)
    .pause(30)
    .exec { session => session.set(Test.TEST_ASSET_ID, assetControllerTest.getAssetID(session(Test.TEST_SELLER_TRADER_ID).as[String], session(Test.TEST_ASSET_TYPE).as[String], session(Test.TEST_ASSET_DESCRIPTION).as[String], session(Test.TEST_QUANTITY_UNIT).as[String], session(Test.TEST_ASSET_QUANTITY).as[String], session(Test.TEST_ASSET_PRICE).as[String])) }
}

object UnmoderatedBuyerExecuteOrder {

  val unmoderatedBuyerExecuteOrder = scenario("unmoderatedBuyerExecuteOrder")
    /* .exec { session => session.set(Test.TEST_ORDER_STATUS, orderControllerTest.getOrderStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
     .doIf(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING) {
       asLongAsDuring(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING, Duration.create(80, "seconds")) {
         pause(1)
           .exec { session => session.set(Test.TEST_ORDER_STATUS, negotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
       }
     }*/
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(orderControllerTest.unmoderatedBuyerExecuteOrderScenario)
    .pause(10)
    .exec(accountControllerTest.logoutScenario)
    .pause(50)
}

object UnmoderatedSellerExecuteOrder {

  val unmoderatedSellerExecuteOrder = scenario("unmoderatedSellerExecuteOrder")
    /* .exec { session => session.set(Test.TEST_ORDER_STATUS, orderControllerTest.getOrderStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
     .doIf(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING) {
       asLongAsDuring(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING, Duration.create(80, "seconds")) {
         pause(1)
           .exec { session => session.set(Test.TEST_ORDER_STATUS, negotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
       }
     }*/
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(orderControllerTest.unmoderatedSellerExecuteOrderScenario)
    .exec(accountControllerTest.logoutScenario)
    .pause(50)
}


