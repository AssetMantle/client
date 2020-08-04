package simulations

import constants.Test
import scenarios._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import feeders.JDBCFeeder._

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

  val issueAssetUnmoderated = scenario("IssueAssetUnmoderated")
    .exec { session => session.set(Test.TEST_SELLER_TRADER_ID, getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_BUYER_TRADER_ID, getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(AssetControllerTest.unmoderatedIssueAssetRequest)
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
    .exec { session => session.set(Test.TEST_ASSET_ID, getAssetID(session(Test.TEST_SELLER_TRADER_ID).as[String], session(Test.TEST_ASSET_TYPE).as[String], session(Test.TEST_ASSET_DESCRIPTION).as[String])) }
}

object UnmoderatedBuyerExecuteOrder {

  val unmoderatedBuyerExecuteOrder = scenario("UnmoderatedBuyerExecuteOrder")
    /* .exec { session => session.set(Test.TEST_ORDER_STATUS, orderControllerTest.getOrderStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
     .doIf(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING) {
       asLongAsDuring(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING, Duration.create(80, "seconds")) {
          .pause(Test.LOOP_DELAY)
           .exec { session => session.set(Test.TEST_ORDER_STATUS, negotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
       }
     }*/
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(OrderControllerTest.unmoderatedBuyerExecuteOrderScenario)
    .pause(10)
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
}

object UnmoderatedSellerExecuteOrder {

  val unmoderatedSellerExecuteOrder = scenario("UnmoderatedSellerExecuteOrder")
    /* .exec { session => session.set(Test.TEST_ORDER_STATUS, orderControllerTest.getOrderStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
     .doIf(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING) {
       asLongAsDuring(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING, Duration.create(80, "seconds")) {
          .pause(Test.LOOP_DELAY)
           .exec { session => session.set(Test.TEST_ORDER_STATUS, negotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
       }
     }*/
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(OrderControllerTest.unmoderatedSellerExecuteOrderScenario)
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
}


