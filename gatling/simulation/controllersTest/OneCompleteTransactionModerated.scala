package controllersTest

import constants.Test
import controllersTest.addOrganizationControllerTest.getOrganizationID
import controllersTest.addZoneControllerTest._
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID

//import controllersTest.changeBuyerBidControllerTest.getBuyerAddress
import feeders._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.Duration

class OneCompleteTransactionModerated extends Simulation {

  val oneCompleteModeratedScenario = scenario("OneCompleteTest")
    .exec(CreateZone.createZone)
    .exec(CreateSellerOrganization.createSellerOrganization)
    .exec(CreateBuyerOrganization.createBuyerOrganization)
    .exec(CreateSeller.createSeller)
    .exec(CreateBuyer.createBuyer)
    .exec(IssueAssetModerated.issueAssetModerated)
    .exec(IssueFiat.issueFiat)
    .exec(ConfirmBuyerBid.confirmBuyerBid)
    .exec(ConfirmSellerBid.confirmSellerBid)
    .exec(SendFiat.sendFiat)
    .exec(ReleaseAsset.releaseAsset)
    .exec(SendAsset.sendAsset)
    .exec(BuyerSellerExecuteOrder.buyerSellerExecuteOrder)
    .exec(SetBuyerFeedback.setBuyerFeedback)
    .exec(SetSellerFeedback.setSellerFeedback)
    .exec(RedeemAsset.redeemAsset)
    .exec(RedeemFiat.redeemFiat)


  setUp(
    oneCompleteModeratedScenario.inject(atOnceUsers(1))
  ).maxDuration(1300)
    .protocols(http.baseUrl(Test.BASE_URL))
}

object CreateZone {

  val createZone = scenario("CREATE ZONE")
    .exec(accountControllerTest.loginMain)
    .exec(addZoneControllerTest.inviteZoneScenario)
    .exec(accountControllerTest.logoutScenario)
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
    .exec(accountControllerTest.loginMain)
    .exec(addZoneControllerTest.verifyZoneScenario)
    .exec(accountControllerTest.logoutScenario)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .doIf(session => session(Test.USER_TYPE).as[String] != "ZONE") {
      asLongAsDuring(session => session(Test.USER_TYPE).as[String] != "ZONE", Duration.create(80, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
      }
    }
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
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String])) }
    .doIf(session => session(Test.TEST_ORGANIZATION_ID).as[String] == "0") {
      asLongAsDuring(session => session(Test.TEST_ORGANIZATION_ID).as[String] == "0", Duration.create(30, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String])) }
      }
    }
    .exec(addOrganizationControllerTest.verifyOrganizationScenario)
    .exec(accountControllerTest.logoutScenario)
    .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String])) }
    .doIf(session => session(Test.USER_TYPE).as[String] != "ORGANIZATION") {
      asLongAsDuring(session => session(Test.USER_TYPE).as[String] != "ORGANIZATION", Duration.create(80, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String])) }
      }
    }
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
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String])) }
    .doIf(session => session(Test.TEST_ORGANIZATION_ID).as[String] == "0") {
      asLongAsDuring(session => session(Test.TEST_ORGANIZATION_ID).as[String] == "0", Duration.create(30, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String])) }
      }
    }
    .exec(addOrganizationControllerTest.verifyOrganizationScenario)
    .exec(accountControllerTest.logoutScenario)
    .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String])) }
    .doIf(session => session(Test.USER_TYPE).as[String] != constants.User.ORGANIZATION) {
      asLongAsDuring(session => session(Test.USER_TYPE).as[String] != constants.User.ORGANIZATION, Duration.create(80, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String])) }
      }
    }
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
    .exec(setACLControllerTest.addTraderRequest)
    .exec(accountControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELL_ORGANIZATION_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(setACLControllerTest.organizationVerifyTrader)
    .exec(accountControllerTest.logoutScenario)
    .exec { session => session.set(Test.TEST_SELLER_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .doIf(session => session(Test.USER_TYPE).as[String] != constants.User.TRADER) {
      asLongAsDuring(session => session(Test.USER_TYPE).as[String] != constants.User.TRADER, Duration.create(80, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
      }
    }

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
    .exec(accountControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUY_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUY_ORGANIZATION_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(setACLControllerTest.organizationVerifyTrader)
    .exec(accountControllerTest.logoutScenario)
    .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .doIf(session => session(Test.USER_TYPE).as[String] != constants.User.TRADER) {
      asLongAsDuring(session => session(Test.USER_TYPE).as[String] != constants.User.TRADER, Duration.create(80, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.USER_TYPE, accountControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
      }
    }
}

object AddCounterParty {
  val addCounterParty = scenario("AddCounterParty")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(session => session.set(Test.TEST_COUNTER_PARTY_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]))
    .exec(traderControllerTest.traderRelationRequestScenario)
    .exec(accountControllerTest.logoutScenario)
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
    .exec { session => session.set(Test.TEST_ASSET_ID, assetControllerTest.getAssetID(session(Test.TEST_SELLER_TRADER_ID).as[String])) }
}

object CreateSalesQuote {
  val createSalesQuote = scenario("CreateSalesQuote")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(negotiationControllerTest.negotiationRequestScenario)
    .exec(accountControllerTest.logoutScenario)
    .exec { session => session.set(Test.TEST_NEGOTIATION_ID, negotiationControllerTest.getNegotiationID(session(Test.TEST_SELLER_TRADER_ID).as[String])) }
}

object AcceptSalesQuoteAndAllTradeTerms {
  val acceptSalesQuoteAndAllTradeTerms = scenario("AcceptSalesQuoteAndAllTradeTerms")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(negotiationControllerTest.acceptNegotiationRequest)
    .exec { session => session.set(Test.TEST_NEGOTIATION_STATUS, negotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
    .doIf(session => session(Test.TEST_NEGOTIATION_STATUS).as[String] != constants.Status.Negotiation.STARTED) {
      asLongAsDuring(session => session(Test.TEST_NEGOTIATION_STATUS).as[String] != constants.Status.Negotiation.STARTED, Duration.create(80, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_NEGOTIATION_STATUS, negotiationControllerTest.getNegotiationStatus(session(Test.TEST_NEGOTIATION_ID).as[String])) }
      }
    }
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
    .exec(negotiationControllerTest.uploadNegotiationDocuments)
    .exec(negotiationControllerTest.uploadAssetDocuments)
    .exec(negotiationControllerTest.uploadNegotiationDocuments)
    .exec(negotiationControllerTest.addBillOfLading)
    .exec(negotiationControllerTest.addContract)
    .exec(negotiationControllerTest.addInvoice)
    .exec(accountControllerTest.logoutScenario)
}

object AcceptBillOfLading{

   val acceptBillOfLading= scenario("AcceptBillOfLading")
     .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
     .exec(accountControllerTest.loginScenario)
     .exec(negotiationControllerTest.acceptBillOfLading)
     .exec(accountControllerTest.logoutScenario)
}

object IssueFiat {

  val issueFiat = scenario("IssueFiat")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(issueFiatControllerTest.issueFiatRequestScenario)
    .exec(accountControllerTest.logoutScenario)
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
}

object ReleaseAsset {

  val releaseAsset = scenario("ReleaseAsset")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(assetControllerTest.releaseAsset)
    .exec(accountControllerTest.logoutScenario)
}

object SendFiat {

  val sendFiat = scenario("SendFiat")
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
    .exec { session =>
      println(session)
      session
    }
}

object ModeratedBuyerAndSellerExecuteOrder {

  val moderatedBuyerAndSellerExecuteOrder = scenario("ModeratedBuyerAndSellerExecuteOrder")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(orderControllerTest.moderatedBuyerExecuteOrderScenario)
    .exec(orderControllerTest.moderatedSellerExecuteOrderScenario)
    .exec(accountControllerTest.logoutScenario)

}

object RedeemAsset {

  val redeemAsset = scenario("RedeemAsset")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(accountControllerTest.loginScenario)
    .exec(assetControllerTest.redeemAsset)
    .exec(accountControllerTest.logoutScenario)

}


