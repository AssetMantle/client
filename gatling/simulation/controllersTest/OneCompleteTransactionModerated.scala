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
    .exec(CreateOrganization.createOrganization)
    .exec(CreateSeller.createSeller)
    .exec(CreateBuyer.createBuyer)
    .exec(IssueAssetModerated.issueAsset)
    .exec(IssueFiat.issueFiat)
    .exec(ChangeBuyerBid.changeBuyerBid)
    .exec(ChangeSellerBid.changeSellerBid)
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

object CreateOrganization {

  val createOrganization = scenario("CREATE ORGANIZATION")
    .feed(OrganizationLoginFeeder.organizationLoginFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ORGANIZATION_PASSWORD).as[String]))
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
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
    .doIf(session => session(Test.TEST_ORGANIZATION_ID).as[String] == "0") {
      asLongAsDuring(session => session(Test.TEST_ORGANIZATION_ID).as[String] == "0", Duration.create(30, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
      }
    }
    .exec(addOrganizationControllerTest.verifyOrganizationScenario)
    .exec(logoutControllerTest.logoutScenario)
    .exec { session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
    .doIf(session => session(Test.USER_TYPE).as[String] != "ORGANIZATION") {
      asLongAsDuring(session => session(Test.USER_TYPE).as[String] != "ORGANIZATION", Duration.create(80, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
      }
    }
}

object CreateSeller {

  val createSeller = scenario("CreateSeller")
    .feed(SellerFeeder.sellerFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(signUpControllerTest.signUpScenario)
    .exec(loginControllerTest.loginScenario)
    .exec(updateContactControllerTest.updateContactScenario)
    .exec(profileControllerTest.addIdentification)
    .exec(setACLControllerTest.addTraderRequest)
    .exec(logoutControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .doIf(session => session(Test.TEST_TRADER_ID).as[String] == "0") {
      asLongAsDuring(session => session(Test.TEST_TRADER_ID).as[String] == "0", Duration.create(30, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])) }
      }
    }
    .exec(session => session.set(Test.TEST_TRADER_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]))
    .exec(setACLControllerTest.zoneVerifyTrader)
    .exec(logoutControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ORGANIZATION_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(setACLControllerTest.organizationVerifyTrader)
    .exec(logoutControllerTest.logoutScenario)
    .exec { session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .doIf(session => session(Test.USER_TYPE).as[String] != "TRADER") {
      asLongAsDuring(session => session(Test.USER_TYPE).as[String] != "TRADER", Duration.create(80, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
      }
    }

}

object CreateBuyer {

  val createBuyer = scenario("CreateBuyer")
    .feed(BuyerFeeder.buyerFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(signUpControllerTest.signUpScenario)
    .exec(loginControllerTest.loginScenario)
    .exec(updateContactControllerTest.updateContactScenario)
    .exec(profileControllerTest.addIdentification)
    .exec(setACLControllerTest.addTraderRequest)
    .exec(logoutControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .doIf(session => session(Test.TEST_TRADER_ID).as[String] == "0") {
      asLongAsDuring(session => session(Test.TEST_TRADER_ID).as[String] == "0", Duration.create(30, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])) }
      }
    }
    .exec(session => session.set(Test.TEST_TRADER_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]))
    .exec(setACLControllerTest.zoneVerifyTrader)
    .exec(logoutControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ORGANIZATION_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(setACLControllerTest.organizationVerifyTrader)
    .exec(logoutControllerTest.logoutScenario)
    .exec { session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .doIf(session => session(Test.USER_TYPE).as[String] != "TRADER") {
      asLongAsDuring(session => session(Test.USER_TYPE).as[String] != "TRADER", Duration.create(80, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
      }
    }
}

object IssueAssetModerated {

  val issueAsset = scenario("IssueAsset")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(issueAssetControllerTest.issueAssetRequestScenario)
    .exec(logoutControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(issueAssetControllerTest.issueAssetScenario)
    .exec(logoutControllerTest.logoutScenario)
}

object IssueFiat {

  val issueFiat = scenario("IssueFiat")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(issueFiatControllerTest.issueFiatRequestScenario)
    .exec(logoutControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(issueFiatControllerTest.issueFiatScenario)
    .exec(logoutControllerTest.logoutScenario)

}

object ChangeBuyerBid {

  val changeBuyerBid = scenario("ChangeBuyerBid")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(session => session.set(Test.TEST_SELLER_ADDRESS, getAddressFromAccountID(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_BUYER_ADDRESS, getAddressFromAccountID(session(Test.TEST_BUYER_USERNAME).as[String])))
    .exec(session => session.set(Test.TEST_PEG_HASH, issueAssetControllerTest.getPegHashByOwnerAddress(session(Test.TEST_SELLER_USERNAME).as[String])))
    .doIf(session => session(Test.TEST_PEG_HASH).as[String] == "0") {
      asLongAsDuring(session => session(Test.TEST_PEG_HASH).as[String] == "0", Duration.create(30, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_PEG_HASH, issueAssetControllerTest.getPegHashByOwnerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
      }
    }
    .exec(changeBuyerBidControllerTest.changeBuyerBidScenario)
    .exec(logoutControllerTest.logoutScenario)
}

object ChangeSellerBid {

  val changeSellerBid = scenario("ChangeSellerBid")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(changeSellerBidControllerTest.changeSellerBidScenario)
    .exec(logoutControllerTest.logoutScenario)
}

object ConfirmBuyerBid {

  val confirmBuyerBid = scenario("ConfirmBuyerBid")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(confirmBuyerBidControllerTest.confirmBuyerBidScenario)
    .exec(logoutControllerTest.logoutScenario)
}

object ConfirmSellerBid {

  val confirmSellerBid = scenario("ConfirmSellerBid")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(confirmSellerBidControllerTest.confirmSellerBidScenario)
    .exec(logoutControllerTest.logoutScenario)
}

object ReleaseAsset {

  val releaseAsset = scenario("ReleaseAsset")

    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(releaseAssetControllerTest.releaseAssetScenario)
    .exec(logoutControllerTest.logoutScenario)
}

object SendFiat {

  val sendFiat = scenario("SendFiat")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(sendFiatControllerTest.sendFiatScenario)
    .exec(logoutControllerTest.logoutScenario)
}


object SendAsset {

  val sendAsset = scenario("SendAsset")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(sendAssetControllerTest.sendAssetScenario)
    .exec(logoutControllerTest.logoutScenario)
    .exec{session=> println(session)
    session}
}

object BuyerSellerExecuteOrder {

  val buyerSellerExecuteOrder = scenario("BuyerSellerExecuteOrder")
    .exec(session => session.set(Test.TEST_NEGOTIATION_REQUEST_ID, confirmBuyerBidControllerTest.getNegotiationRequestIDFromSellerAccountID(session(Test.TEST_SELLER_USERNAME).as[String])))
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(buyerExecuteOrderControllerTest.moderatedBuyerExecuteOrderScenario)
    .exec(sellerExecuteOrderControllerTest.moderatedSellerExecuteOrder)
    .exec(logoutControllerTest.logoutScenario)

}

object SetBuyerFeedback {

  val setBuyerFeedback = scenario("SetBuyerFeedback")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(setBuyerFeedbackControllerTest.setBuyerFeedbackScenario)
    .exec(logoutControllerTest.logoutScenario)
}

object SetSellerFeedback {

  val setSellerFeedback = scenario("SetSellerFeedback")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(setSellerFeedbackControllerTest.setSellerFeedbackScenario)
    .exec(logoutControllerTest.logoutScenario)
}

object RedeemAsset {

  val redeemAsset = scenario("RedeemAsset")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_BUYER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(redeemAssetControllerTest.redeemAssetScenario)
    .exec(logoutControllerTest.logoutScenario)

}

object RedeemFiat {
  val redeemFiat = scenario("RedeemFiat")
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
    .exec(redeemFiatControllerTest.redeemFiatScenario)
    .exec(logoutControllerTest.logoutScenario)
}


