package controllersTest

import constants.Test
import controllersTest.addOrganizationControllerTest.getOrganizationID
import controllersTest.addZoneControllerTest._

//import controllersTest.changeBuyerBidControllerTest.getBuyerAddress
import feeders._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class OneCompleteTransactionModerated extends Simulation {

  val oneCompleteModeratedScenario = scenario("OneCompleteTest")
    .exec(CreateZone.createZone)
    .exec(CreateSellerOrganization.createSellerOrganization)
    .exec(CreateBuyerOrganization.createBuyerOrganization)
    .exec(CreateSeller.createSeller)
    .exec(CreateBuyer.createBuyer)
    .exec(AddCounterParty.addCounterParty)
    .exec(IssueFiat.issueFiat)
    .repeat(10) {
      exec(IssueAssetModerated.issueAssetModerated)
        .exec(CreateSalesQuote.createSalesQuote)
        .exec(AcceptSalesQuoteAndAllTradeTerms.acceptSalesQuoteAndAllTradeTerms)
        .exec(UploadContractAndOtherTradeDocuments.uploadContractAndOtherTradeDocuments)
        .exec(AcceptBillOfLading.acceptBillOfLading)
        .exec(VesselCheckAndReleaseAsset.vesselCheckAndReleaseAsset)
        .exec(BuyerConfirmNegotiation.buyerConfirmNegotiation)
        .exec(SellerConfirmNegotiation.sellerConfirmNegotiation)
        .exec(SendFiat.sendFiat)
        .exec(SendAsset.sendAsset)
        .exec(ModeratedBuyerAndSellerExecuteOrder.moderatedBuyerAndSellerExecuteOrder)
        .exec(RedeemAsset.redeemAsset)
    }


  setUp(
    oneCompleteModeratedScenario.inject(atOnceUsers(1))
  ).protocols(http.baseUrl(Test.BASE_URL))
}

object CreateZone {

  val createZone = scenario("CREATE ZONE")
    .feed(ZoneLoginFeeder.zoneLoginFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(accountControllerTest.signUpScenario)
    .exec(accountControllerTest.loginScenario)
    .exec(contactControllerTest.addMobileNumberScenario)
    .exec(contactControllerTest.verifyMobileNumberScenario)
    .exec(contactControllerTest.addEmailAddressScenario)
    .exec(contactControllerTest.verifyEmailAddressScenario)
    .exec(accountControllerTest.addIdentification)
    .exec(addZoneControllerTest.addZoneRequestScenario)
    .exec(accountControllerTest.logoutScenario)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .exec(accountControllerTest.loginMain)
    .exec(addZoneControllerTest.verifyZoneScenario)
    .pause(20)
  /*.exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
  .doIf(session => session(Test.USER_TYPE).as[String] != constants.User.ZONE) {
    asLongAsDuring(session => session(Test.USER_TYPE).as[String] != constants.User.ZONE, Duration.create(80, "seconds")) {
      pause(1)
        .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
    }
  }*/
}

object CreateSellerOrganization {

  val createSellerOrganization = scenario("CREATE SELLER ORGANIZATION")
    .feed(SellOrganizationLoginFeeder.sellOrganizationLoginFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELL_ORGANIZATION_PASSWORD).as[String]))
    .exec(accountControllerTest.signUpScenario)
    .exec(accountControllerTest.loginScenario)
    .exec(contactControllerTest.addMobileNumberScenario)
    .exec(contactControllerTest.verifyMobileNumberScenario)
    .exec(contactControllerTest.addEmailAddressScenario)
    .exec(contactControllerTest.verifyEmailAddressScenario)
    .exec(accountControllerTest.addIdentification)
    .exec(addOrganizationControllerTest.addOrganizationRequestScenario)
    .exec(accountControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec { session => session.set(Test.TEST_SELL_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, session(Test.TEST_SELL_ORGANIZATION_ID).as[String]) }
    .exec(backgroundCheckControllerTest.corporateScan)
    .exec(addOrganizationControllerTest.verifyOrganizationScenario)
    .exec(accountControllerTest.logoutScenario)
    .pause(20)
  /* .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String])) }
   .doIf(session => session(Test.USER_TYPE).as[String] != constants.User.ORGANIZATION) {
     asLongAsDuring(session => session(Test.USER_TYPE).as[String] != constants.User.ORGANIZATION, Duration.create(80, "seconds")) {
       pause(1)
         .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String])) }
     }
   }*/
}

object CreateSeller {

  val createSeller = scenario("CreateSeller")
    .feed(SellerFeeder.sellerFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(accountControllerTest.signUpScenario)
    .exec(accountControllerTest.loginScenario)
    .exec(contactControllerTest.addMobileNumberScenario)
    .exec(contactControllerTest.verifyMobileNumberScenario)
    .exec(contactControllerTest.addEmailAddressScenario)
    .exec(contactControllerTest.verifyEmailAddressScenario)
    .exec(accountControllerTest.addIdentification)
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, session(Test.TEST_SELL_ORGANIZATION_ID).as[String]) }
    .exec(setACLControllerTest.addTraderRequest)
    .exec(accountControllerTest.logoutScenario)
    .exec { session => session.set(Test.TEST_SELLER_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_TRADER_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]) }
    .exec { session => session.set(Test.TEST_TRADER_ID, session(Test.TEST_SELLER_TRADER_ID).as[String]) }
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELL_ORGANIZATION_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(setACLControllerTest.organizationVerifyTrader)
    .exec(accountControllerTest.logoutScenario)
    .pause(20)
  /* .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
   .doIf(session => session(Test.USER_TYPE).as[String] != constants.User.TRADER) {
     asLongAsDuring(session => session(Test.USER_TYPE).as[String] != constants.User.TRADER, Duration.create(80, "seconds")) {
       pause(1)
         .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
     }
   }*/
}


object CreateBuyerOrganization {

  val createBuyerOrganization = scenario("CREATE BUYER ORGANIZATION")
    .feed(BuyOrganizationLoginFeeder.buyOrganizationLoginFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUY_ORGANIZATION_PASSWORD).as[String]))
    .exec(accountControllerTest.signUpScenario)
    .exec(accountControllerTest.loginScenario)
    .exec(contactControllerTest.addMobileNumberScenario)
    .exec(contactControllerTest.verifyMobileNumberScenario)
    .exec(contactControllerTest.addEmailAddressScenario)
    .exec(contactControllerTest.verifyEmailAddressScenario)
    .exec(accountControllerTest.addIdentification)
    .exec(addOrganizationControllerTest.addOrganizationRequestScenario)
    .exec(accountControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec { session => session.set(Test.TEST_BUY_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, session(Test.TEST_BUY_ORGANIZATION_ID).as[String]) }
    .exec(backgroundCheckControllerTest.corporateScan)
    .exec(addOrganizationControllerTest.verifyOrganizationScenario)
    .exec(accountControllerTest.logoutScenario)
    .pause(20)
  /*.exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String])) }
  .doIf(session => session(Test.USER_TYPE).as[String] != constants.User.ORGANIZATION) {
    asLongAsDuring(session => session(Test.USER_TYPE).as[String] != constants.User.ORGANIZATION, Duration.create(80, "seconds")) {
      pause(1)
        .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String])) }
    }
  }*/
}


object CreateBuyer {

  val createBuyer = scenario("CreateBuyer")
    .feed(BuyerFeeder.buyerFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(accountControllerTest.signUpScenario)
    .exec(accountControllerTest.loginScenario)
    .exec(contactControllerTest.addMobileNumberScenario)
    .exec(contactControllerTest.verifyMobileNumberScenario)
    .exec(contactControllerTest.addEmailAddressScenario)
    .exec(contactControllerTest.verifyEmailAddressScenario)
    .exec(accountControllerTest.addIdentification)
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, session(Test.TEST_BUY_ORGANIZATION_ID).as[String]) }
    .exec(setACLControllerTest.addTraderRequest)
    .exec(accountControllerTest.logoutScenario)
    .exec { session => session.set(Test.TEST_BUYER_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])).set(Test.TEST_TRADER_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]) }
    .exec { session => session.set(Test.TEST_TRADER_ID, session(Test.TEST_BUYER_TRADER_ID).as[String]) }
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUY_ORGANIZATION_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(setACLControllerTest.organizationVerifyTrader)
    .exec(accountControllerTest.logoutScenario)
    .pause(20)
  /*.exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
  .doIf(session => session(Test.USER_TYPE).as[String] != constants.User.TRADER) {
    asLongAsDuring(session => session(Test.USER_TYPE).as[String] != constants.User.TRADER, Duration.create(80, "seconds")) {
      pause(1)
        .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
    }
  }*/
}

object AddCounterParty {
  val addCounterParty = scenario("AddCounterParty")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(session => session.set(Test.TEST_COUNTER_PARTY_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]))
    .exec(traderControllerTest.traderRelationRequestScenario)
    .exec(accountControllerTest.logoutScenario)
    .exec(session => session.set(Test.FROM_ID, session(Test.TEST_SELLER_TRADER_ID).as[String]).set(Test.TO_ID, session(Test.TEST_BUYER_TRADER_ID).as[String]))
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(traderControllerTest.acceptTraderRelation)
    .exec(accountControllerTest.logoutScenario)
}

object IssueAssetModerated {

  val issueAssetModerated = scenario("IssueAssetModerated")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(assetControllerTest.moderatedIssueAssetRequestScenario)
    .exec(accountControllerTest.logoutScenario)
    .pause(5)
    .exec { session =>
      println(session)
      session
    }
    .exec { session => session.set(Test.TEST_ASSET_ID, assetControllerTest.getAssetID(session(Test.TEST_SELLER_TRADER_ID).as[String], session(Test.TEST_ASSET_TYPE).as[String], session(Test.TEST_ASSET_DESCRIPTION).as[String], session(Test.TEST_QUANTITY_UNIT).as[String], session(Test.TEST_ASSET_QUANTITY).as[String], session(Test.TEST_ASSET_PRICE).as[String])) }
}

object CreateSalesQuote {
  val createSalesQuote = scenario("CreateSalesQuote")
    .exec { session => session.set(Test.TEST_COUNTER_PARTY, session(Test.TEST_BUYER_TRADER_ID).as[String]) }
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(negotiationControllerTest.negotiationRequestScenario)
    .exec(accountControllerTest.logoutScenario)

}

object AcceptSalesQuoteAndAllTradeTerms {
  val acceptSalesQuoteAndAllTradeTerms = scenario("AcceptSalesQuoteAndAllTradeTerms")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(negotiationControllerTest.acceptNegotiationRequest)
    .pause(20)
    /* .exec { session => session.set(Test.TEST_NEGOTIATION_STATUS, negotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
     .doIf(session => session(Test.TEST_NEGOTIATION_STATUS).as[String] != constants.Status.Negotiation.STARTED) {
       asLongAsDuring(session => session(Test.TEST_NEGOTIATION_STATUS).as[String] != constants.Status.Negotiation.STARTED, Duration.create(80, "seconds")) {
         pause(1)
           .exec { session => session.set(Test.TEST_NEGOTIATION_STATUS, negotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
       }
     }*/
    .exec(negotiationControllerTest.acceptNegotiationTerms)
    .exec(negotiationControllerTest.confirmAllNegotiationTerms)
    .exec(accountControllerTest.logoutScenario)
}

object UploadContractAndOtherTradeDocuments {

  val uploadContractAndOtherTradeDocuments = scenario("UploadContractAndOtherTradeDocuments")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(negotiationControllerTest.uploadContract)
    .exec(negotiationControllerTest.updateContractSigned)
    .exec(negotiationControllerTest.uploadAssetDocuments)
    .exec(negotiationControllerTest.addBillOfLading)
    .exec(negotiationControllerTest.uploadNegotiationDocuments)
    .exec(accountControllerTest.logoutScenario)
}

object AcceptBillOfLading {

  val acceptBillOfLading = scenario("AcceptBillOfLading")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(negotiationControllerTest.acceptBillOfLading)
    .exec(accountControllerTest.logoutScenario)
}

object IssueFiat {

  val issueFiat = scenario("IssueFiat")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec { session => session.set(Test.TEST_TRADER_ID, session(Test.TEST_BUYER_TRADER_ID).as[String]) }
    .exec(issueFiatControllerTest.issueFiatRequestScenario)
    .exec(accountControllerTest.logoutScenario)
    .pause(20)
    .exec(issueFiatControllerTest.westernUnionRTCB)
}

object BuyerConfirmNegotiation {

  val buyerConfirmNegotiation = scenario("BuyerConfirmNegotiation")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(negotiationControllerTest.buyerConfirmNegotiation)
    .exec(accountControllerTest.logoutScenario)
}

object SellerConfirmNegotiation {

  val sellerConfirmNegotiation = scenario("SellerConfirmNegotiation")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(negotiationControllerTest.sellerConfirmNegotiation)
    .exec(accountControllerTest.logoutScenario)
    .pause(30)
}

object VesselCheckAndReleaseAsset {

  val vesselCheckAndReleaseAsset = scenario("VesselCheckAndReleaseAsset")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(backgroundCheckControllerTest.vesselScan)
    .exec(assetControllerTest.releaseAsset)
    .exec(accountControllerTest.logoutScenario)
    .pause(30)
}

object SendFiat {

  val sendFiat = scenario("SendFiat")
    /*.exec { session => session.set(Test.TEST_NEGOTIATION_STATUS, negotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
    .doIf(session => session(Test.TEST_NEGOTIATION_STATUS).as[String] != constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED) {
      asLongAsDuring(session => session(Test.TEST_NEGOTIATION_STATUS).as[String] != constants.Status.Negotiation.STARTED, Duration.create(80, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_NEGOTIATION_STATUS, negotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
      }
    }*/
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(sendFiatControllerTest.sendFiatScenario)
    .exec(accountControllerTest.logoutScenario)
}

object SendAsset {

  val sendAsset = scenario("SendAsset")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(assetControllerTest.sendAsset)
    .exec(accountControllerTest.logoutScenario)
    .pause(30)
}

object ModeratedBuyerAndSellerExecuteOrder {

  val moderatedBuyerAndSellerExecuteOrder = scenario("ModeratedBuyerAndSellerExecuteOrder")
    /* .exec { session => session.set(Test.TEST_ORDER_STATUS, orderControllerTest.getOrderStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
     .doIf(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING) {
       asLongAsDuring(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING, Duration.create(80, "seconds")) {
         pause(1)
           .exec { session => session.set(Test.TEST_ORDER_STATUS, negotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
       }
     }*/
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(orderControllerTest.moderatedBuyerExecuteOrderScenario)
    .pause(10)
    .exec(orderControllerTest.moderatedSellerExecuteOrderScenario)
    .exec(accountControllerTest.logoutScenario)
    .pause(30)

}

object RedeemAsset {

  val redeemAsset = scenario("RedeemAsset")
    /* .exec { session => session.set(Test.TEST_ORDER_STATUS, orderControllerTest.getOrderStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
     .doIf(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.COMPLETED) {
       asLongAsDuring(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.COMPLETED, Duration.create(80, "seconds")) {
         pause(1)
           .exec { session => session.set(Test.TEST_ORDER_STATUS, negotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
       }
     }*/
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(assetControllerTest.redeemAsset)
    .exec(accountControllerTest.logoutScenario)
    .exec { session =>
      println(session)
      session
    }

}


