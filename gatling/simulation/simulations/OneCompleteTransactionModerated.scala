package simulations

import constants.Test
import controllers.routes
import scenarios._
import feeders.JDBCFeeder._
import feeders._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.Duration

class OneCompleteTransactionModerated extends Simulation {

  val zoneUsername = "ZONE1381bpw3XasE"
  val zonePassword = "123123123"

  val oneCompleteModeratedScenario = scenario("OneCompleteTest")
    /*.exec(CreateZone.createZone)*/
    .exec(session => session.set(Test.TEST_ZONE_USERNAME, zoneUsername).set(Test.TEST_ZONE_PASSWORD, zonePassword))
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .exec(CreateSellerOrganization.createSellerOrganization)
    .exec(CreateBuyerOrganization.createBuyerOrganization)
    .exec(CreateSeller.createSeller)
    .exec(CreateBuyer.createBuyer)
    .exec(AddCounterParty.addCounterParty)
    .exec(IssueFiat.issueFiat)
    .exec(IssueAssetModerated.issueAssetModerated)
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
    .exec(RedeemFiat.redeemFiat)


  setUp(
    oneCompleteModeratedScenario.inject(atOnceUsers(300)
    )).protocols(http.baseUrl(Test.BASE_URL))
}

object CreateZone {

  val createZone = scenario("CREATE ZONE")
    .exec(AccountControllerTest.loginMain)
    .exec(AddZoneControllerTest.inviteZoneScenario)
    .feed(ZoneLoginFeeder.zoneLoginFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(AccountControllerTest.signUpScenario)
    .exec(AccountControllerTest.loginScenario)
    .exec(AddZoneControllerTest.acceptZoneInviteScenario)
    .exec(ContactControllerTest.addOrUpdateMobileNumberScenario)
    .exec(ContactControllerTest.verifyMobileNumberScenario)
    .exec(ContactControllerTest.addOrUpdateEmailAddressScenario)
    .exec(ContactControllerTest.verifyEmailAddressScenario)
    .exec(AccountControllerTest.addIdentification)
    .exec(AddZoneControllerTest.addZoneRequestScenario)
    .exec(AccountControllerTest.logoutScenario)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .exec(AccountControllerTest.loginMain)
    .exec(AddZoneControllerTest.verifyZoneScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
  /*.exec { session => session.set(Test.USER_TYPE, AccountControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
  .doIf(session => session(Test.USER_TYPE).as[String] != constants.User.ZONE) {
    asLongAsDuring(session => session(Test.USER_TYPE).as[String] != constants.User.ZONE, Duration.create(80, "seconds")) {
       .pause(Test.LOOP_DELAY)
        .exec { session => session.set(Test.USER_TYPE, AccountControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
    }
  }*/
}

object CreateSellerOrganization {

  val createSellerOrganization = scenario("CREATE SELLER ORGANIZATION")
    .feed(SellOrganizationLoginFeeder.sellOrganizationLoginFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELL_ORGANIZATION_PASSWORD).as[String]))
    .exec(AccountControllerTest.signUpScenario)
    .exec(AccountControllerTest.loginScenario)
    .exec(ContactControllerTest.addOrUpdateMobileNumberScenario)
    .exec(ContactControllerTest.verifyMobileNumberScenario)
    .exec(ContactControllerTest.addOrUpdateEmailAddressScenario)
    .exec(ContactControllerTest.verifyEmailAddressScenario)
    .exec(AccountControllerTest.addIdentification)
    .exec(AddOrganizationControllerTest.addOrganizationRequestScenario)
    .exec(AccountControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec { session => session.set(Test.TEST_SELL_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, session(Test.TEST_SELL_ORGANIZATION_ID).as[String]) }
    .exec(BackgroundCheckControllerTest.corporateScan)
    .exec(AddOrganizationControllerTest.verifyOrganizationScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
  /* .exec { session => session.set(Test.USER_TYPE, AccountControllerTest.getUserType(session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String])) }
   .doIf(session => session(Test.USER_TYPE).as[String] != constants.User.ORGANIZATION) {
     asLongAsDuring(session => session(Test.USER_TYPE).as[String] != constants.User.ORGANIZATION, Duration.create(80, "seconds")) {
        .pause(Test.LOOP_DELAY)
         .exec { session => session.set(Test.USER_TYPE, AccountControllerTest.getUserType(session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String])) }
     }
   }*/
}

object CreateSeller {

  val createSeller = scenario("CreateSeller")
    .feed(SellerFeeder.sellerFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.signUpScenario)
    .exec(AccountControllerTest.loginScenario)
    .exec(ContactControllerTest.addOrUpdateMobileNumberScenario)
    .exec(ContactControllerTest.verifyMobileNumberScenario)
    .exec(ContactControllerTest.addOrUpdateEmailAddressScenario)
    .exec(ContactControllerTest.verifyEmailAddressScenario)
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, session(Test.TEST_SELL_ORGANIZATION_ID).as[String]) }
    .exec(SetACLControllerTest.addTraderRequest)
    .exec(AccountControllerTest.logoutScenario)
    .exec { session => session.set(Test.TEST_SELLER_TRADER_ID, getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_TRADER_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]) }
    .exec { session => session.set(Test.TEST_TRADER_ID, session(Test.TEST_SELLER_TRADER_ID).as[String]) }
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELL_ORGANIZATION_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(SetACLControllerTest.organizationVerifyTrader)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
  /* .exec { session => session.set(Test.USER_TYPE, AccountControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
   .doIf(session => session(Test.USER_TYPE).as[String] != constants.User.TRADER) {
     asLongAsDuring(session => session(Test.USER_TYPE).as[String] != constants.User.TRADER, Duration.create(80, "seconds")) {
        .pause(Test.LOOP_DELAY)
         .exec { session => session.set(Test.USER_TYPE, AccountControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
     }
   }*/
}


object CreateBuyerOrganization {

  val createBuyerOrganization = scenario("CREATE BUYER ORGANIZATION")
    .feed(BuyOrganizationLoginFeeder.buyOrganizationLoginFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUY_ORGANIZATION_PASSWORD).as[String]))
    .exec(AccountControllerTest.signUpScenario)
    .exec(AccountControllerTest.loginScenario)
    .exec(ContactControllerTest.addOrUpdateMobileNumberScenario)
    .exec(ContactControllerTest.verifyMobileNumberScenario)
    .exec(ContactControllerTest.addOrUpdateEmailAddressScenario)
    .exec(ContactControllerTest.verifyEmailAddressScenario)
    .exec(AccountControllerTest.addIdentification)
    .exec(AddOrganizationControllerTest.addOrganizationRequestScenario)
    .exec(AccountControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec { session => session.set(Test.TEST_BUY_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, session(Test.TEST_BUY_ORGANIZATION_ID).as[String]) }
    .exec(BackgroundCheckControllerTest.corporateScan)
    .exec(AddOrganizationControllerTest.verifyOrganizationScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
  /*.exec { session => session.set(Test.USER_TYPE, AccountControllerTest.getUserType(session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String])) }
  .doIf(session => session(Test.USER_TYPE).as[String] != constants.User.ORGANIZATION) {
    asLongAsDuring(session => session(Test.USER_TYPE).as[String] != constants.User.ORGANIZATION, Duration.create(80, "seconds")) {
       .pause(Test.LOOP_DELAY)
        .exec { session => session.set(Test.USER_TYPE, AccountControllerTest.getUserType(session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String])) }
    }
  }*/
}


object CreateBuyer {

  val createBuyer = scenario("CreateBuyer")
    .feed(BuyerFeeder.buyerFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(AccountControllerTest.signUpScenario)
    .exec(AccountControllerTest.loginScenario)
    .exec(ContactControllerTest.addOrUpdateMobileNumberScenario)
    .exec(ContactControllerTest.verifyMobileNumberScenario)
    .exec(ContactControllerTest.addOrUpdateEmailAddressScenario)
    .exec(ContactControllerTest.verifyEmailAddressScenario)
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, session(Test.TEST_BUY_ORGANIZATION_ID).as[String]) }
    .exec(SetACLControllerTest.addTraderRequest)
    .exec(AccountControllerTest.logoutScenario)
    .exec { session => session.set(Test.TEST_BUYER_TRADER_ID, getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])).set(Test.TEST_TRADER_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]) }
    .exec { session => session.set(Test.TEST_TRADER_ID, session(Test.TEST_BUYER_TRADER_ID).as[String]) }
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUY_ORGANIZATION_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(SetACLControllerTest.organizationVerifyTrader)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY+50)
 /* .exec { session => session.set(Test.USER_TYPE, getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
  .doIf(session => session(Test.USER_TYPE).as[String] != constants.User.TRADER) {
    asLongAsDuring(session => session(Test.USER_TYPE).as[String] != constants.User.TRADER, Duration.create(150, "seconds")) {
       pause(Test.LOOP_DELAY)

        .exec { session =>
          println(session(Test.TEST_BUYER_USERNAME).as[String]+"----------"+session(Test.USER_TYPE).as[String])
          session.set(Test.USER_TYPE, getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
    }
  }*/
}

object AddCounterParty {
  val addCounterParty = scenario("AddCounterParty")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(session => session.set(Test.TEST_COUNTER_PARTY_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]))
    .exec(TraderControllerTest.traderRelationRequestScenario)
    .exec(AccountControllerTest.logoutScenario)
    .exec(session => session.set(Test.FROM_ID, session(Test.TEST_SELLER_TRADER_ID).as[String]).set(Test.TO_ID, session(Test.TEST_BUYER_TRADER_ID).as[String]))
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(TraderControllerTest.acceptTraderRelation)
    .exec(AccountControllerTest.logoutScenario)
}

object IssueFiat {

  val issueFiat = scenario("IssueFiat")
    /*.exec(session => session.set(Test.TEST_SELLER_USERNAME, "SELL10cEUVnPV9").set(Test.TEST_SELLER_PASSWORD,"SELL10cEUVnPV9"))
        .exec(session => session.set(Test.TEST_BUYER_USERNAME, "BUY1281yUKOdVIf").set(Test.TEST_BUYER_PASSWORD, "BUY1281yUKOdVIf"))
        .exec { session => session.set(Test.TEST_SELLER_TRADER_ID, getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])) }
        .exec { session => session.set(Test.TEST_BUYER_TRADER_ID, getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])) }
   */ /* .feed(BuyerFeeder.buyerFeed)
     .exec { session => session.set(Test.TEST_BUYER_TRADER_ID, getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])) }*/
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec { session => session.set(Test.TEST_TRADER_ID, session(Test.TEST_BUYER_TRADER_ID).as[String]) }
    .exec(WesternUnionControllerTest.issueFiatRequestScenario)
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.REQUEST_DELAY)
    .exec(WesternUnionControllerTest.westernUnionRTCB)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
}

object IssueAssetModerated {

  val issueAssetModerated = scenario("IssueAssetModerated")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(AssetControllerTest.moderatedIssueAssetRequest)
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
    .exec { session => session.set(Test.TEST_ASSET_ID, getAssetID(session(Test.TEST_SELLER_TRADER_ID).as[String], session(Test.TEST_ASSET_TYPE).as[String], session(Test.TEST_ASSET_DESCRIPTION).as[String])) }
}

object CreateSalesQuote {
  val createSalesQuote = scenario("CreateSalesQuote")
    .exec { session => session.set(Test.TEST_COUNTER_PARTY, session(Test.TEST_BUYER_TRADER_ID).as[String]) }
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(NegotiationControllerTest.negotiationRequestScenario)
    .exec(NegotiationControllerTest.addPaymentTerms)
    .exec(NegotiationControllerTest.addDocumentList)
    .exec(NegotiationControllerTest.reviewNegotiationRequest)
    .exec(AccountControllerTest.logoutScenario)
}

object AcceptSalesQuoteAndAllTradeTerms {
  val acceptSalesQuoteAndAllTradeTerms = scenario("AcceptSalesQuoteAndAllTradeTerms")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(NegotiationControllerTest.acceptNegotiationRequest)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
    /* .exec { session => session.set(Test.TEST_NEGOTIATION_STATUS, NegotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
     .doIf(session => session(Test.TEST_NEGOTIATION_STATUS).as[String] != constants.Status.Negotiation.STARTED) {
       asLongAsDuring(session => session(Test.TEST_NEGOTIATION_STATUS).as[String] != constants.Status.Negotiation.STARTED, Duration.create(80, "seconds")) {
          .pause(Test.LOOP_DELAY)
           .exec { session => session.set(Test.TEST_NEGOTIATION_STATUS, NegotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
       }
     }*/
    .exec(NegotiationControllerTest.acceptNegotiationTerms)
    .exec(NegotiationControllerTest.confirmAllNegotiationTerms)
    .exec(AccountControllerTest.logoutScenario)
}

object UploadContractAndOtherTradeDocuments {

  val uploadContractAndOtherTradeDocuments = scenario("UploadContractAndOtherTradeDocuments")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(NegotiationControllerTest.uploadContract)
    .exec(NegotiationControllerTest.updateContractSigned)
    .exec(AssetControllerTest.uploadAssetDocuments)
    .exec(AssetControllerTest.addBillOfLading)
    .exec(NegotiationControllerTest.uploadNegotiationDocuments)
    .exec(AccountControllerTest.logoutScenario)
}

object AcceptBillOfLading {

  val acceptBillOfLading = scenario("AcceptBillOfLading")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(AssetControllerTest.updateBillOfLadingStatus(true))
    .exec(AccountControllerTest.logoutScenario)
}

object BuyerConfirmNegotiation {

  val buyerConfirmNegotiation = scenario("BuyerConfirmNegotiation")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(NegotiationControllerTest.buyerConfirmNegotiation(constants.Test.PRECONDITIONS_SATISFIED))
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
}

object SellerConfirmNegotiation {

  val sellerConfirmNegotiation = scenario("SellerConfirmNegotiation")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(NegotiationControllerTest.sellerConfirmNegotiation(constants.Test.PRECONDITIONS_SATISFIED))
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
}

object VesselCheckAndReleaseAsset {

  val vesselCheckAndReleaseAsset = scenario("VesselCheckAndReleaseAsset")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(BackgroundCheckControllerTest.vesselScan)
    .exec(AssetControllerTest.releaseAsset)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
}

object SendFiat {

  val sendFiat = scenario("SendFiat")
    /*.exec { session => session.set(Test.TEST_NEGOTIATION_STATUS, NegotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
    .doIf(session => session(Test.TEST_NEGOTIATION_STATUS).as[String] != constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED) {
      asLongAsDuring(session => session(Test.TEST_NEGOTIATION_STATUS).as[String] != constants.Status.Negotiation.STARTED, Duration.create(80, "seconds")) {
         .pause(Test.LOOP_DELAY)
          .exec { session => session.set(Test.TEST_NEGOTIATION_STATUS, NegotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
      }
    }*/
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(SendFiatControllerTest.sendFiatScenario)
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
}

object SendAsset {

  val sendAsset = scenario("SendAsset")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(AssetControllerTest.sendAsset(constants.Test.PRECONDITIONS_SATISFIED))
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
}

object ModeratedBuyerAndSellerExecuteOrder {

  val moderatedBuyerAndSellerExecuteOrder = scenario("ModeratedBuyerAndSellerExecuteOrder")
    /* .exec { session => session.set(Test.TEST_ORDER_STATUS, orderControllerTest.getOrderStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
     .doIf(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING) {
       asLongAsDuring(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING, Duration.create(80, "seconds")) {
          .pause(Test.LOOP_DELAY)
           .exec { session => session.set(Test.TEST_ORDER_STATUS, NegotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
       }
     }*/
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(OrderControllerTest.moderatedBuyerExecuteOrderScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
    .exec(OrderControllerTest.moderatedSellerExecuteOrderScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY+20)

}

object RedeemAsset {

  val redeemAsset = scenario("RedeemAsset")
    /* .exec { session => session.set(Test.TEST_ORDER_STATUS, orderControllerTest.getOrderStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
     .doIf(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.COMPLETED) {
       asLongAsDuring(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.COMPLETED, Duration.create(80, "seconds")) {
          .pause(Test.LOOP_DELAY)
           .exec { session => session.set(Test.TEST_ORDER_STATUS, NegotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
       }
     }*/
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(AssetControllerTest.redeemAsset)
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
}


object RedeemFiat {

  val redeemFiat = scenario("RedeemFiat")
    /* .exec { session => session.set(Test.TEST_ORDER_STATUS, orderControllerTest.getOrderStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
     .doIf(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.COMPLETED) {
       asLongAsDuring(session => session(Test.TEST_ORDER_STATUS).as[String] != constants.Status.Order.COMPLETED, Duration.create(80, "seconds")) {
          .pause(Test.LOOP_DELAY)
           .exec { session => session.set(Test.TEST_ORDER_STATUS, NegotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
       }
     }*/
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .feed(RedeemAmountFeeder.redeemAmountFeed)
    .exec(RedeemFiatControllerTest.redeemFiatScenario)
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
}



