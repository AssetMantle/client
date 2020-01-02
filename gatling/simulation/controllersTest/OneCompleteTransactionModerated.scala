package controllersTest

import constants.Test
import controllersTest.addOrganizationControllerTest.getOrganizationID
import controllersTest.addZoneControllerTest.getZoneID
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID

//import controllersTest.changeBuyerBidControllerTest.getBuyerAddress
import feeders._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.Duration

class OneCompleteTransactionModerated extends Simulation {


  /* val oneCompleteTransactionModeratedCombined=scenario("ONE_COMPLETE_TRANSACTION_MODERATED")
     .feed(ZoneLoginFeeder.zoneLoginFeed)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_ZONE_PASSWORD).as[String]))
     .exec(signUpControllerTest.signUpScenario)
     .exec(loginControllerTest.loginScenario)
     .exec(sendCoinControllerTest.FaucetRequestScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(sendCoinControllerTest.approveFaucetRequestScenario)
     .exec(loginControllerTest.loginScenario)
     .exec(addZoneControllerTest.addZoneRequestScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec{session=> session.set(Test.TEST_ZONE_ID, getZoneID("${%s}".format(Test.TEST_ZONE_ACCOUNT_ID)))}
     .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String]))}
     .doIf(session=> session(Test.TEST_ZONE_ID).as[String] == "0") {
       asLongAs(session=> session(Test.TEST_ZONE_ID).as[String] == "0") {
         pause(1)
           .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String]))}
       }
     }
     .exec(loginControllerTest.loginMain)
     .exec(addZoneControllerTest.verifyZoneScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
     .doIf(session=> session(Test.USER_TYPE).as[String] != "ZONE") {
       asLongAs(session=> session(Test.USER_TYPE).as[String] != "ZONE") {
         pause(1)
           .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String]))}
       }
     }
     .feed(OrganizationLoginFeeder.organizationLoginFeed)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_ORGANIZATION_PASSWORD).as[String]))
     .exec(signUpControllerTest.signUpScenario)
     .exec(loginControllerTest.loginScenario)
     .exec(sendCoinControllerTest.FaucetRequestScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(sendCoinControllerTest.approveFaucetRequestScenario)
     .exec(loginControllerTest.loginScenario)
     .exec(addOrganizationControllerTest.addOrganizationRequestScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_ZONE_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_ORGANIZATION_USERNAME).as[String]))}
     .doIf(session=> session(Test.TEST_ORGANIZATION_ID).as[String] == "0") {
       asLongAs(session=> session(Test.TEST_ORGANIZATION_ID).as[String] == "0") {
         pause(1)
           .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_ORGANIZATION_USERNAME).as[String]))}
       }
     }
     .exec(addOrganizationControllerTest.verifyOrganizationScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
     .doIf(session=> session(Test.USER_TYPE).as[String] != "ORGANIZATION") {
       asLongAs(session=> session(Test.USER_TYPE).as[String] != "ORGANIZATION") {
         pause(1)
           .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ORGANIZATION_USERNAME).as[String]))}
       }
     }
     .feed(SellerFeeder.sellerFeed)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_SELLER_PASSWORD).as[String]))
     .exec(signUpControllerTest.signUpScenario)
     .exec(loginControllerTest.loginScenario)
     .exec(sendCoinControllerTest.FaucetRequestScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(sendCoinControllerTest.approveFaucetRequestScenario)
     .exec(loginControllerTest.loginScenario)
     .exec(setACLControllerTest.addTraderRequest)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_ZONE_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])) }
     .doIf(session => session(Test.TEST_TRADER_ID).as[String] == "0") {
       asLongAs(session => session(Test.TEST_TRADER_ID).as[String] == "0") {
         pause(1)
           .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])) }
       }
     }
     .exec(session=>session.set(Test.TEST_TRADER_USERNAME,session(Test.TEST_SELLER_USERNAME).as[String]))
     .exec(setACLControllerTest.zoneVerifyTrader)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_ORGANIZATION_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(setACLControllerTest.organizationVerifyTrader)
     .exec(logoutControllerTest.logoutScenario)
     .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
     .doIf(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
       asLongAs(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
         pause(1)
           .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String]))}
       }
     }
     .feed(BuyerFeeder.buyerFeed)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_BUYER_PASSWORD).as[String]))
     .exec(signUpControllerTest.signUpScenario)
     .exec(loginControllerTest.loginScenario)
     .exec(sendCoinControllerTest.FaucetRequestScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(sendCoinControllerTest.approveFaucetRequestScenario)
     .exec(loginControllerTest.loginScenario)
     .exec(setACLControllerTest.addTraderRequest)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_ZONE_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])) }
     .doIf(session => session(Test.TEST_TRADER_ID).as[String] == "0") {
       asLongAs(session => session(Test.TEST_TRADER_ID).as[String] == "0") {
         pause(1)
           .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])) }
       }
     }
     .exec(session=>session.set(Test.TEST_TRADER_USERNAME,session(Test.TEST_BUYER_USERNAME).as[String]))
     .exec(setACLControllerTest.zoneVerifyTrader)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_ORGANIZATION_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(setACLControllerTest.organizationVerifyTrader)
     .exec(logoutControllerTest.logoutScenario)
     .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
     .doIf(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
       asLongAs(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
         pause(1)
           .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String]))}
       }
     }
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_SELLER_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(issueAssetControllerTest.issueAssetRequestScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_ZONE_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(issueAssetControllerTest.issueAssetScenario)
     .exec(logoutControllerTest.logoutScenario)


     .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
     .doIf(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
       asLongAs(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
         pause(1)
           .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
       }
     }
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_BUYER_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(issueFiatControllerTest.issueFiatRequestScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_ZONE_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(issueFiatControllerTest.issueFiatScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_BUYER_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(session=> session.set(Test.TEST_SELLER_ADDRESS,getAddressFromAccountID(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_BUYER_ADDRESS,getAddressFromAccountID(session(Test.TEST_BUYER_USERNAME).as[String])))
     .exec(session=>session.set(Test.TEST_PEG_HASH,issueAssetControllerTest.getPegHashByOwnerAddress(session(Test.TEST_SELLER_USERNAME).as[String])))
     .doIf(session=> session(Test.TEST_PEG_HASH).as[String] == "0") {
       asLongAs(session=> session(Test.TEST_PEG_HASH).as[String] == "0") {
         pause(1)
           .exec { session => session.set(Test.TEST_PEG_HASH,issueAssetControllerTest.getPegHashByOwnerAddress(session(Test.TEST_SELLER_USERNAME).as[String]))}
       }
     }
     .exec(changeBuyerBidControllerTest.changeBuyerBidScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_SELLER_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(changeSellerBidControllerTest.changeSellerBidScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_BUYER_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(confirmBuyerBidControllerTest.confirmBuyerBidScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_SELLER_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(confirmSellerBidControllerTest.confirmSellerBidScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_BUYER_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(sendFiatControllerTest.sendFiatScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_ZONE_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(releaseAssetControllerTest.releaseAssetScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_SELLER_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(sendAssetControllerTest.sendAssetScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=> session.set(Test.TEST_NEGOTIATION_REQUEST_ID,confirmBuyerBidControllerTest.getNegotiationRequestIDFromSellerAccountID(session(Test.TEST_SELLER_USERNAME).as[String])))
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_ZONE_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(buyerExecuteOrderControllerTest.buyerExecuteOrderScenario)
     .exec(sellerExecuteOrderControllerTest.moderatedSellerExecuteOrder)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_BUYER_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(setBuyerFeedbackControllerTest.setBuyerFeedbackScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_SELLER_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(setSellerFeedbackControllerTest.setSellerFeedbackScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_BUYER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_BUYER_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(redeemAssetControllerTest.redeemAssetScenario)
     .exec(logoutControllerTest.logoutScenario)
     .exec(session=>session.set(Test.TEST_USERNAME,session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD,session(Test.TEST_SELLER_PASSWORD).as[String]))
     .exec(loginControllerTest.loginScenario)
     .exec(redeemFiatControllerTest.redeemFiatScenario)
     .exec(logoutControllerTest.logoutScenario)
 */

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

  /* val oneCompleteTransactionModerated=scenario("ONE_COMPLETE_TRANSACTION_MODERATED")
     .exec(ZoneSignUp.masterZoneSignUp)
     .exec(ZoneFaucetRequest.masterLoginAndFaucetRequestZone)
     .exec(loginMainApproveFaucetZone.masterLoginMainAndApproveFaucetRequestZone)
     .exec(ZoneLoginAddRequest.masterLoginAddZoneRequest)
     .exec(LoginMainAndApproveZone.masterLoginMainAndApproveZone)
     .exec(OrganizationSignUp.masterOrganizationSignUp)
     .exec(OrganizationFaucetRequest.masterLoginAndFaucetRequestOrganization)
     .exec(LoginMainApproveFaucetOrganization.masterLoginMainAndApproveFaucetRequestOrganization)
     .exec(OrganizationLoginAddRequest.masterOrganizationLoginAddRequest)
     .exec(LoginZoneAndApproveOrganization.masterLoginZoneAndApproveOrganization)
     .exec(SignUpAndLoginSellerAndCreateFaucetRequest.signUpAndLoginSellerAndCreateFaucetRequest)
     .exec(ApproveFaucetRequestSeller.approveFaucetRequestSeller)
     .exec(AddSellerTraderRequest.addSellerTraderRequest)
     .exec(ApproveSellerTraderACL.approveSellerTraderACL)
     .exec(SignUpAndLoginBuyerAndCreateFaucetRequest.signUpAndLoginBuyerAndCreateFaucetRequest)
     .exec(ApproveFaucetRequestBuyer.approveFaucetRequestBuyer)
     .exec(AddBuyerTraderRequest.addBuyerTraderRequest)
     .exec(ApproveBuyerTraderACL.approveBuyerTraderACL)
     .exec(IssueAssetRequestForSeller.issueAssetRequestForSeller)
     .exec(ApproveIssueAsset.approveIssueAsset)
     .exec(IssueFiatRequestBuyer.issueFiatRequest)
     .exec(IssueFiat.issueFiat)
     .exec(ChangeBuyerBid.changeBuyerBid)
     .exec(ChangeSellerBid.changeSellerBid)
     .exec(ConfirmBuyerBid.confirmBuyerBid)
     .exec(ConfirmSellerBid.confirmSellerBid)
     .exec(SendFiat.sendFiat)
     .exec(ReleaseAsset.releaseAsset)
     .exec(SendAsset.sendAsset)
     .exec(BuyerAndSellerExecuteOrder.buyerAndSellerExecuteOrder)
     .exec(SetBuyerFeedback.setBuyerFeedback)
     .exec(SetSellerFeedback.setSellerFeedback)
     .exec(RedeemAsset.redeemAsset)
     .exec(RedeemFiat.redeemFiat)*/


  setUp(
    oneCompleteModeratedScenario.inject(atOnceUsers(1))
  ).maxDuration(1300)
    .protocols(http.baseUrl(Test.BASE_URL))

}


object CreateZone {

  val createZone = scenario("CREATE ZONE")
    .feed(ZoneLoginFeeder.zoneLoginFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(signUpControllerTest.signUpScenario)
    .exec(loginControllerTest.loginScenario)
    .exec(sendCoinControllerTest.FaucetRequestScenario)
    .exec(logoutControllerTest.logoutScenario)
    .exec(sendCoinControllerTest.approveFaucetRequestScenario)
    .exec(loginControllerTest.loginScenario)
    .exec(addZoneControllerTest.addZoneRequestScenario)
    .exec(logoutControllerTest.logoutScenario)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID("${%s}".format(Test.TEST_ZONE_ACCOUNT_ID))) }
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .doIf(session => session(Test.TEST_ZONE_ID).as[String] == "0") {
      asLongAsDuring(session => session(Test.TEST_ZONE_ID).as[String] == "0", Duration.create(30, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
      }
    }
    .exec(loginControllerTest.loginMain)
    .exec(addZoneControllerTest.verifyZoneScenario)
    .exec(logoutControllerTest.logoutScenario)
    .exec { session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .doIf(session => session(Test.USER_TYPE).as[String] != "ZONE") {
      asLongAsDuring(session => session(Test.USER_TYPE).as[String] != "ZONE", Duration.create(80, "seconds")) {
        pause(1)
          .exec { session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
      }
    }
}

object CreateOrganization {

  val createOrganization = scenario("CREATE ORGANIZATION")
    .feed(OrganizationLoginFeeder.organizationLoginFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ORGANIZATION_PASSWORD).as[String]))
    .exec(signUpControllerTest.signUpScenario)
    .exec(loginControllerTest.loginScenario)
    .exec(sendCoinControllerTest.FaucetRequestScenario)
    .exec(logoutControllerTest.logoutScenario)
    .exec(sendCoinControllerTest.approveFaucetRequestScenario)
    .exec(loginControllerTest.loginScenario)
    .exec(addOrganizationControllerTest.addOrganizationRequestScenario)
    .exec(logoutControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_ZONE_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_ZONE_PASSWORD).as[String]))
    .exec(loginControllerTest.loginScenario)
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
    .exec(sendCoinControllerTest.FaucetRequestScenario)
    .exec(logoutControllerTest.logoutScenario)
    .exec(sendCoinControllerTest.approveFaucetRequestScenario)
    .exec(loginControllerTest.loginScenario)
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
    .exec(sendCoinControllerTest.FaucetRequestScenario)
    .exec(logoutControllerTest.logoutScenario)
    .exec(sendCoinControllerTest.approveFaucetRequestScenario)
    .exec(loginControllerTest.loginScenario)
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


/*


object ZoneSignUp {
  val masterZoneSignUp: ScenarioBuilder = scenario("masterZoneSignUp")
    .feed(ZoneLoginFeeder.zoneLoginFeed)
    .exec(http("Zone_SignUp_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.SIGN_UP.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Zone_SignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.USERNAME_AVAILABLE -> true,
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.CONFIRM_PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(css("div:contains(%s)".format(constants.FormField.CONFIRM_NOTE_NEW_KEY_DETAILS.name)).exists)
      //  .check(css(s"div:contains('${constants.FormField.CONFIRM_NOTE_NEW_KEY_DETAILS.name}')").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(regex("""blockchainAddress=([^&]*)""").saveAs(Test.TEST_ZONE_ADDRESS))
      .check(regex("""publicKey=([^&]*)""").saveAs(Test.TEST_PUBLIC_KEY))
      .check(regex("""seed=([^"]*)""").saveAs(Test.TEST_SEED))
    )
    .pause(1)
    .exec(http("Note_New_Key_Details_Zone")
      .post(session=> routes.AccountController.noteNewKeyDetails(session(Test.TEST_ZONE_USERNAME).as[String],session(Test.TEST_ZONE_ADDRESS).as[String],session(Test.TEST_PUBLIC_KEY).as[String],session(Test.TEST_SEED).as[String].replace('+',' ')).url)
      .formParamMap(Map(
        Form.CONFIRM_NOTE_NEW_KEY_DETAILS-> true,
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS SIGNED_UP").exists)
    )
    .pause(3)
}

object ZoneFaucetRequest {
  val masterLoginAndFaucetRequestZone: ScenarioBuilder = scenario("masterLoginAndFaucetRequestZone")
    .exec(http("Login_Zone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Login_Zone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(bodyString.saveAs("LoginBody"))
      .check(substring("${%s}".format(Test.TEST_ZONE_USERNAME)).exists)
    )
    .pause(3)
    .exec{session=>println(session)
      session}
    .feed(CouponFeeder.couponFeed)
    .exec(http("Faucet_Request_Zone_GET")
      .get(routes.SendCoinController.faucetRequestForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.FAUCET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Faucet_Request_Zone_POST")
      .post(routes.SendCoinController.faucetRequest().url)
      .formParamMap(Map(
        Form.COUPON -> "${%s}".format(Test.TEST_COUPON),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS COINS_REQUESTED").exists)
    )
    .pause(2)
    .exec(http("Zone_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Zone_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object loginMainApproveFaucetZone {

  val masterLoginMainAndApproveFaucetRequestZone: ScenarioBuilder = scenario("masterLoginMainAndApproveFaucetRequestZone")
    .feed(GenesisFeeder.genesisFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("Main_Login_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Main_Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("${%s}".format(Test.TEST_MAIN_USERNAME)).exists)
    )
    .pause(2)
    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .doIf(session=> session(Test.TEST_REQUEST_ID).as[String] == "0") {
      asLongAsDuring(session=> session(Test.TEST_REQUEST_ID).as[String] =="0",Duration.create(30,"seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_ZONE_USERNAME).as[String])) }
      }
    }

    .exec(http("Approve_Zone_Faucet_Request_GET")
      .get(session=>routes.SendCoinController.approveFaucetRequestsForm(session(Test.TEST_REQUEST_ID).as[String],session(Test.TEST_ZONE_USERNAME).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.APPROVE_FAUCET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    /*.exec(
      http("ApproveFaucetRequestZone_GET")
      .get("/master/approveFaucetRequests?requestID=" + "${%s}".format(Test.TEST_REQUEST_ID) + "&accountID=" + "${%s}".format(Test.TEST_ZONE_USERNAME))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))*/
    .pause(2)
    .exec(http("Approve_Zone_Faucet_Request_POST")
      .post(routes.SendCoinController.approveFaucetRequests().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS FAUCET_REQUEST_APPROVED").exists)
    )
    .pause(3)
    .exec(http("Main_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Main_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)

    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "USER") {
      asLongAsDuring(session=> session(Test.USER_TYPE).as[String] != "USER",Duration.create(80,"seconds")) {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
      }
    }
}


object ZoneLoginAddRequest {

  val masterLoginAddZoneRequest: ScenarioBuilder = scenario("masterLoginAddZoneRequest")
    .exec(http("Zone_Login_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Zone_Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->"${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .feed(NameFeeder.nameFeed)
    .feed(CurrencyFeeder.currencyFeed)
    .feed(AddressDataFeeder.addressDataFeed)
    .exec(http("Add_Zone_Form_GET")
      .get(routes.AddZoneController.addZoneForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.ADD_ZONE.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Add_Zone_POST")
      .post(routes.AddZoneController.addZone().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.NAME -> "${%s}".format(Test.TEST_NAME),
        Form.CURRENCY -> "${%s}".format(Test.TEST_CURRENCY),
        Form.ADDRESS_ADDRESS_LINE_1 -> "${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Form.ADDRESS_ADDRESS_LINE_2 -> "${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Form.ADDRESS_LANDMARK -> "${%s}".format(Test.TEST_LANDMARK),
        Form.ADDRESS_CITY -> "${%s}".format(Test.TEST_CITY),
        Form.ADDRESS_COUNTRY ->"${%s}".format(Test.TEST_COUNTRY),
        Form.ADDRESS_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Form.ADDRESS_PHONE -> "${%s}".format(Test.TEST_PHONE)
      ))
      .check(substring("ZONE_KYC_FILES").exists)
    )
    .pause(2)
    .foreach(addZoneControllerTest.zoneKYCs,"documentType"){
      feed(ImageFeeder.imageFeed)
        .exec(http("ZoneKYC_Upload_"+"${documentType}"+"_Form_GET")
          .get(session=>routes.AddZoneController.userUploadZoneKYCForm(session("documentType").as[String]).url)
          .check(substring("BROWSE").exists)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(1)
        .exec(http("ZoneKYC_Upload_"+"${documentType}")
          .post(session=>routes.AddZoneController.userUploadZoneKYC(session("documentType").as[String]).url)
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file",Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("Store_ZoneKYC_"+"${documentType}")
            .get(session=>routes.AddZoneController.userStoreZoneKYC(session(Test.TEST_FILE_NAME).as[String],session("documentType").as[String]).url)
            .check(substring("ZONE_KYC_FILES").exists)
        )
        .pause(2)
    }
    .pause(1)
    .exec(http("User_Review_Add_Zone_Request_Form")
      .get(routes.AddZoneController.userReviewAddZoneRequestForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.REVIEW_ADD_ZONE_ON_COMPLETION.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("User_Review_Add_Zone_Request")
      .post(routes.AddZoneController.userReviewAddZoneRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
      .check(substring("SUCCESS ZONE_ADDED_FOR_VERIFICATION").exists)
    )
    .pause(2)
    .exec(http("Logout_Zone_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Logout_Zone_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}


object LoginMainAndApproveZone {

  val masterLoginMainAndApproveZone: ScenarioBuilder = scenario("masterLoginMainAndApproveZone")
    .feed(GenesisFeeder.genesisFeed)
    .exec(http("Main_Login_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Main_Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String]))}
    .doIf(session=> session(Test.TEST_ZONE_ID).as[String] == "0") {
      asLongAsDuring(session=> session(Test.TEST_ZONE_ID).as[String] == "0",Duration.create(30,"seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String]))}
      }
    }
    .exec(http("Get_Pending_Verify_Zone_Request")
      .get(routes.AddZoneController.viewPendingVerifyZoneRequests().url)
      .check(substring("${%s}".format(Test.TEST_ZONE_ID)).exists)
    )
    .pause(1)
    .foreach(addZoneControllerTest.zoneKYCs,"documentType"){
      exec(http("ZoneKYC_Update_Status_For_"+"${documentType}")
        .post(routes.AddZoneController.updateZoneKYCDocumentStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(2)
    }
    .exec(http("Verify_Zone_GET")
      .get(session=>routes.AddZoneController.verifyZoneForm(session(Test.TEST_ZONE_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.VERIFY_ZONE.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Verify_Zone_POST")
      .post(routes.AddZoneController.verifyZone().url)
      .formParamMap(Map(
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ZONE_VERIFIED").exists)
    )
    .pause(3)
    .exec(http("Main_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Main_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "ZONE") {
      asLongAsDuring(session=> session(Test.USER_TYPE).as[String] != "ZONE",Duration.create(80,"seconds")) {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ZONE_USERNAME).as[String]))}
      }
    }
}

object OrganizationSignUp {

  val masterOrganizationSignUp: ScenarioBuilder = scenario("masterOrganizationSignUp")
    .feed(OrganizationLoginFeeder.organizationLoginFeed)
    .exec(http("Organization_SignUp_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.SIGN_UP.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Organization_SignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
        Form.USERNAME_AVAILABLE -> true,
        Form.PASSWORD -> "${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
        Form.CONFIRM_PASSWORD ->"${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(css("div:contains(%s)".format(constants.FormField.CONFIRM_NOTE_NEW_KEY_DETAILS.name)).exists)
      //  .check(css(s"div:contains('${constants.FormField.CONFIRM_NOTE_NEW_KEY_DETAILS.name}')").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(regex("""blockchainAddress=([^&]*)""").saveAs(Test.TEST_ORGANIZATION_ADDRESS))
      .check(regex("""publicKey=([^&]*)""").saveAs(Test.TEST_PUBLIC_KEY))
      .check(regex("""seed=([^"]*)""").saveAs(Test.TEST_SEED))
    )
    .pause(1)
    .exec(http("Note_New_Key_Details_Organization")
      .post(session=> routes.AccountController.noteNewKeyDetails(session(Test.TEST_ORGANIZATION_USERNAME).as[String],session(Test.TEST_ORGANIZATION_ADDRESS).as[String],session(Test.TEST_PUBLIC_KEY).as[String],session(Test.TEST_SEED).as[String].replace('+',' ')).url)
      .formParamMap(Map(
        Form.CONFIRM_NOTE_NEW_KEY_DETAILS-> true,
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS SIGNED_UP").exists)
    )
    .pause(3)
}

object OrganizationFaucetRequest {

  val masterLoginAndFaucetRequestOrganization: ScenarioBuilder = scenario("masterLoginAndFaucetRequestOrganization")
    .exec(http("Login_Organization_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Login_Organization_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
        Form.PASSWORD ->"${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .feed(CouponFeeder.couponFeed)
    .exec(http("Faucet_Request_Organization_GET")
      .get(routes.SendCoinController.faucetRequestForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.FAUCET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Faucet_Request_Organization_POST")
      .post(routes.SendCoinController.faucetRequest().url)
      .formParamMap(Map(
        Form.COUPON -> "${%s}".format(Test.TEST_COUPON),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS COINS_REQUESTED").exists)
    )
    .pause(2)
    .exec(http("Logout_Zone_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}


object LoginMainApproveFaucetOrganization {

  val masterLoginMainAndApproveFaucetRequestOrganization: ScenarioBuilder = scenario("masterLoginMainAndApproveFaucetRequestOrganization")
    .feed(GenesisFeeder.genesisFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("MainLogin_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("MainLogin_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(2)
    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
    .doIf(session=> session(Test.TEST_REQUEST_ID).as[String] == "0") {
      asLongAsDuring(session=> session(Test.TEST_REQUEST_ID).as[String] == "0",Duration.create(30,"seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_REQUEST_ID,getRequestIDForFaucetRequest(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
      }
    }
    .exec(http("Approve_Faucet_Request_Organization_GET")
      .get(session=>routes.SendCoinController.approveFaucetRequestsForm(session(Test.TEST_REQUEST_ID).as[String],session(Test.TEST_ORGANIZATION_USERNAME).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.APPROVE_FAUCET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Approve_Faucet_Request_Organization_POST")
      .post(routes.SendCoinController.approveFaucetRequests().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS FAUCET_REQUEST_APPROVED").exists)
    )
    .pause(2)
    .exec(http("Main_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Main_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "USER") {
      asLongAsDuring(session=> session(Test.USER_TYPE).as[String] != "USER",Duration.create(80,"seconds")) {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
      }
    }
}


object OrganizationLoginAddRequest {
  val masterOrganizationLoginAddRequest: ScenarioBuilder = scenario("masterOrganizationLoginAddRequest")
    .exec(http("Organization_Login_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Organization_Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .feed(NameFeeder.nameFeed)
    .feed(AddressFeeder.addressFeed)
    .feed(EmailAddressFeeder.emailAddressFeed)
    .feed(AddressDataFeeder.addressDataFeed)
    .exec(http("Add_Organization_GET")
      .get(routes.AddOrganizationController.addOrganizationForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.ADD_ORGANIZATION.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Add_Organization_POST")
      .post(routes.AddOrganizationController.addOrganizationForm().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.NAME -> "${%s}".format(Test.TEST_NAME),
        Form.ABBREVIATION -> "${%s}".format(Test.TEST_NAME),
        Form.ESTABLISHMENT_DATE -> "2019-11-11",
        Form.EMAIL_ADDRESS -> "absd@wfkawf.vom",
        Form.REGISTERED_ADDRESS_LINE_1 -> "${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Form.REGISTERED_ADDRESS_LINE_2 -> "${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Form.REGISTERED_LANDMARK -> "${%s}".format(Test.TEST_LANDMARK),
        Form.REGISTERED_CITY -> "${%s}".format(Test.TEST_CITY),
        Form.REGISTERED_COUNTRY -> "${%s}".format(Test.TEST_COUNTRY),
        Form.REGISTERED_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Form.REGISTERED_PHONE ->"1234567890",
        Form.POSTAL_ADDRESS_LINE_1 ->"${%s}".format(Test.TEST_ADDRESS_LINE_1),
        Form.POSTAL_ADDRESS_LINE_2 ->"${%s}".format(Test.TEST_ADDRESS_LINE_2),
        Form.POSTAL_LANDMARK ->"${%s}".format(Test.TEST_LANDMARK),
        Form.POSTAL_CITY -> "${%s}".format(Test.TEST_CITY),
        Form.POSTAL_COUNTRY -> "${%s}".format(Test.TEST_COUNTRY),
        Form.POSTAL_ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE),
        Form.POSTAL_PHONE -> "${%s}".format(Test.TEST_PHONE)
      ))
      .check(css("legend:contains(%s)".format(constants.Form.USER_UPDATE_UBOS.legend)).exists)
    )
    .pause(2)
    .exec(http("Organization_Bank_Account_Detail_Form_GET")
      .get(routes.AddOrganizationController.organizationBankAccountDetailForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.ORGANIZATION_BANK_ACCOUNT_DETAIL.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(AccountNumberFeeder.accountNumberFeed)
    .feed(SwiftCodeFeeder.swiftCodeFeed)
    .exec(http("Organization_Bank_Account_Detail_POST")
      .post(routes.AddOrganizationController.organizationBankAccountDetail().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_HOLDER_NAME -> "${%s}".format(Test.TEST_NAME),
        Form.NICK_NAME -> "${%s}".format(Test.TEST_NAME),
        Form.ACCOUNT_NUMBER -> "${%s}".format(Test.TEST_ACCOUNT_NUMBER),
        Form.BANK_NAME -> "${%s}".format(Test.TEST_NAME),
        Form.SWIFT_CODE -> "${%s}".format(Test.TEST_SWIFT_CODE),
        Form.STREET_ADDRESS -> "${%s}".format(Test.TEST_NAME),
        Form.COUNTRY -> "${%s}".format(Test.TEST_COUNTRY),
        Form.ZIP_CODE -> "${%s}".format(Test.TEST_ZIP_CODE)
      ))
      .check(substring("ORGANIZATION_KYC_FILES").exists)
    )
    .pause(2)
    .foreach(addOrganizationControllerTest.organizationKYCs,"documentType"){
      feed(ImageFeeder.imageFeed)
        .exec(http("Organization_Kyc_Upload_"+"${documentType}"+"_FORM")
          .get(session=> routes.AddOrganizationController.userUploadOrganizationKYCForm(session("documentType").as[String]).url )
          .check(substring("BROWSE").exists)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(2)
        .exec(http("Organization_Kyc_Upload_"+"${documentType}")

          .post(session=> routes.AddOrganizationController.userUploadOrganizationKYC(session("documentType").as[String]).url)
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("Store_Organization_"+"${documentType}")
            .get(session=>routes.AddOrganizationController.userStoreOrganizationKYC(session(Test.TEST_FILE_NAME).as[String],session("documentType").as[String]).url)
            .check(substring("ORGANIZATION_KYC_FILES").exists)
        )
        .pause(2)
    }
    .exec(http("User_Review_Add_Organization_Request_Form_GET")
      .get(routes.AddOrganizationController.userReviewAddOrganizationRequestForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.USER_REVIEW_ADD_ORGANIZATION_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("User_Review_Add_Organization_Request_POST")
      .post(routes.AddOrganizationController.userReviewAddOrganizationRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
      .check(substring("SUCCESS ORGANIZATION_ADDED_FOR_VERIFICATION").exists)
    )
    .pause(3)
    .exec(http("Organization_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Organization_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}


object LoginZoneAndApproveOrganization {

  val masterLoginZoneAndApproveOrganization: ScenarioBuilder = scenario("masterLoginZoneAndApproveOrganization")
    .feed(OrganizationIDFeeder.organizationIDFeed)
    .exec(http("Zone_Login_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Zone_Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(2)
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_ORGANIZATION_USERNAME).as[String]))}
    .doIf(session=> session(Test.TEST_ORGANIZATION_ID).as[String] == "0") {
      asLongAsDuring(session=> session(Test.TEST_ORGANIZATION_ID).as[String] == "0",Duration.create(30,"seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_ORGANIZATION_USERNAME).as[String]))}
      }
    }
    .exec(http("Get_Pending_Verify_Organization_Request")
      .get(routes.AddOrganizationController.viewPendingVerifyOrganizationRequests().url)
      .check(substring("${%s}".format(Test.TEST_ORGANIZATION_ID)).exists)
    )
    .pause(2)
    .foreach(addOrganizationControllerTest.organizationKYCs,"documentType"){
      exec(http("Organization_KYC_Update_Status"+"${documentType}")
        .post(routes.AddOrganizationController.updateOrganizationKYCDocumentStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(1)
    }
    .exec(http("Verify_Organization_Form_GET")
      .get(session=>routes.AddOrganizationController.verifyOrganizationForm(session(Test.TEST_ORGANIZATION_ID).as[String],session(Test.TEST_ZONE_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.VERIFY_ORGANIZATION.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Verify_Organization_Post")
      .post(routes.AddOrganizationController.verifyOrganization().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
      ))
      .check(substring("SUCCESS ORGANIZATION_VERIFIED").exists)
    ).pause(2)
    .exec(http("Logout_Zone_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Logout_Zone_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "ORGANIZATION") {
      asLongAsDuring(session=> session(Test.USER_TYPE).as[String] != "ORGANIZATION",Duration.create(80,"seconds")) {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_ORGANIZATION_USERNAME).as[String]))}
      }
    }
}

object SignUpAndLoginSellerAndCreateFaucetRequest{

  val signUpAndLoginSellerAndCreateFaucetRequest=scenario("createSeller")
    .feed(SellerFeeder.sellerFeed)
    .exec(http("Seller_SignUp_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.SIGN_UP.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Seller_SignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.USERNAME_AVAILABLE -> true,
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.CONFIRM_PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(css("div:contains(%s)".format(constants.FormField.CONFIRM_NOTE_NEW_KEY_DETAILS.name)).exists)
      //.check(css(s"div:contains('${constants.FormField.CONFIRM_NOTE_NEW_KEY_DETAILS.name}')").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(regex("""blockchainAddress=([^&]*)""").saveAs(Test.TEST_SELLER_ADDRESS))
      .check(regex("""publicKey=([^&]*)""").saveAs(Test.TEST_PUBLIC_KEY))
      .check(regex("""seed=([^"]*)""").saveAs(Test.TEST_SEED))
    )
    .pause(1)
    .exec(http("Note_New_Key_Details_Seller")
      .post(session=> routes.AccountController.noteNewKeyDetails(session(Test.TEST_SELLER_USERNAME).as[String],session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PUBLIC_KEY).as[String],session(Test.TEST_SEED).as[String].replace('+',' ')).url)
      .formParamMap(Map(
        Form.CONFIRM_NOTE_NEW_KEY_DETAILS-> true,
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS SIGNED_UP").exists)
    )
    .pause(3)
    .exec(http("Login_Seller_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Seller_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(2)
    .feed(CouponFeeder.couponFeed)
    .exec(http("Faucet_Request_Seller_GET")
      .get(routes.SendCoinController.faucetRequestForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.FAUCET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Faucet_Request_Seller_POST")
      .post(routes.SendCoinController.faucetRequest().url)
      .formParamMap(Map(
        Form.COUPON -> "${%s}".format(Test.TEST_COUPON),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS COINS_REQUESTED").exists)
    )
    .pause(2)
    .exec(http("Seller_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Seller_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object ApproveFaucetRequestSeller{

  val approveFaucetRequestSeller=scenario("masterLoginMainAndApproveFaucetRequestSeller")
    .feed(GenesisFeeder.genesisFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("Main_Login_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Main_Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(2)
    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .doIf(session=> session(Test.TEST_REQUEST_ID).as[String] == "0") {
      asLongAsDuring(session=> session(Test.TEST_REQUEST_ID).as[String] =="0",Duration.create(30,"seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_SELLER_USERNAME).as[String])) }
      }
    }
    .exec(http("Approve_Faucet_Request_Seller_GET")
      .get(session=>routes.SendCoinController.approveFaucetRequestsForm(session(Test.TEST_REQUEST_ID).as[String],session(Test.TEST_SELLER_USERNAME).as[String]).url)
      // .get("/master/approveFaucetRequests?requestID=" + "${%s}".format(Test.TEST_REQUEST_ID) + "&accountID=" + "${%s}".format(Test.TEST_SELLER_USERNAME))
      .check(css("legend:contains(%s)".format(constants.Form.APPROVE_FAUCET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Approve_Faucet_Request_Zone_POST")
      .post(routes.SendCoinController.approveFaucetRequests().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS FAUCET_REQUEST_APPROVED").exists)
    )
    .pause(3)
    .exec(http("Logout_Main_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Logout_Main_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "USER") {
      asLongAsDuring(session=> session(Test.USER_TYPE).as[String] != "USER",Duration.create(80,"seconds")) {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
      }
    }
}


object AddSellerTraderRequest{

  val addSellerTraderRequest=scenario("AddSellerAsTraderRequest")
    .exec(http("Login_Seller_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Seller_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(2)
    .exec(http("Add_Seller_As_Trader_Form_GET")
      .get(routes.SetACLController.addTraderForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.ADD_TRADER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(NameFeeder.nameFeed)
    .exec(http("Add_Seller_As_Trader_Form_POST")
      .post(routes.SetACLController.addTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN-> "${%s}".format(Form.CSRF_TOKEN),
        Form.ZONE_ID-> "${%s}".format(Test.TEST_ZONE_ID),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.NAME -> "${%s}".format(Test.TEST_NAME)
      ))
      .check(substring("TRADER_KYC_FILES").exists)
    )
    .pause(2)
    .foreach(setACLControllerTest.traderKYCs,"documentType"){
      feed(ImageFeeder.imageFeed)
        .exec(http("Seller_Kyc_Upload_"+"${documentType}"+"_FORM")
          .get(session=>routes.SetACLController.userUploadTraderKYCForm(session("documentType").as[String]).url)
          .check(substring("BROWSE").exists)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(2)
        .exec(http("Seller_Kyc_Upload_"+"${documentType}")
          // .post("/master/userUploadTraderKYC?documentType="+"${documentType}")
          .post(session=> routes.SetACLController.userUploadTraderKYC(session("documentType").as[String]).url)
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("Store_Seller_KYC_"+"${documentType}")

            .get(session=>routes.SetACLController.userStoreTraderKYC(session(Test.TEST_FILE_NAME).as[String],session("documentType").as[String]).url)
            .check(substring("TRADER_KYC_FILES").exists)
        )
        .pause(2)
    }
    .exec(http("User_Review_Add_Seller_Request_Form")
      .get(routes.SetACLController.userReviewAddTraderRequestForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.REVIEW_ADD_TRADER_ON_COMPLETION.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("User_Review_Add_Seller_Request")
      .post(routes.SetACLController.userReviewAddTraderRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
      .check(substring("SUCCESS TRADER_ADDED_FOR_VERIFICATION").exists)
    ).pause(2)
    .exec(http("Logout_Seller_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Logout_Seller_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object ApproveSellerTraderACL {

  val approveSellerTraderACL = scenario("Approve_Seller_ACL")
    .exec(http("LoginZone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("LoginZone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .doIf(session => session(Test.TEST_TRADER_ID).as[String] == "0") {
      asLongAsDuring(session => session(Test.TEST_TRADER_ID).as[String] == "0",Duration.create(30,"seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])) }
      }
    }
    .exec(http("Get_Pending_Zone_Verify_Trader_Request")
      .get(routes.SetACLController.zoneViewPendingVerifyTraderRequests().url)
      .check(substring("${%s}".format(Test.TEST_TRADER_ID)).exists)
    )
    .pause(2)
    .foreach(setACLControllerTest.traderKYCs,"documentType"){
      exec(http("Trader_KYC_Zone_Update_Status_For"+"${documentType}")
        .post(routes.SetACLController.updateTraderKYCDocumentZoneStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.TRADER_ID -> "${%s}".format(Test.TEST_TRADER_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(1)
    }
    .exec(http("Zone_Verify_Seller_Form_GET")
      .get(session=>routes.SetACLController.zoneVerifyTraderForm(session(Test.TEST_SELLER_USERNAME).as[String],session(Test.TEST_ORGANIZATION_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.ZONE_VERIFY_TRADER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Zone_Verify_Seller_POST")
      .post(routes.SetACLController.zoneVerifyTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_ID-> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ISSUE_ASSET_ACL -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT_ACL -> setACLPrivileges.issueFiat, Form.SEND_ASSET_ACL -> setACLPrivileges.sendAsset, Form.SEND_FIAT_ACL -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET_ACL -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT_ACL -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER_ACL -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER_ACL -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID_ACL -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID_ACL -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID_ACL -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID_ACL -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION_ACL -> setACLPrivileges.negotiation, Form.RELEASE_ASSET_ACL -> setACLPrivileges.releaseAsset,
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD)
      ))
      .check(substring("ALL_KYC_FILES_NOT_VERIFIED").exists)
      .check(status.is(412))
    )
    .pause(2)
    .exec(http("Logout_Zone_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Logout_Zone_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
    .exec(http("LoginOrganization_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("LoginOrganization_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .exec(http("Get_Organization_Pending_Verify_Trader_Request")
      .get(routes.SetACLController.organizationViewPendingVerifyTraderRequests().url)
      .check(substring("${%s}".format(Test.TEST_TRADER_ID)).exists)
    )
    .pause(2)
    .foreach(setACLControllerTest.traderKYCs,"documentType"){
      exec(http("Organization_Trader_KYC_update_Status_For"+"${documentType}")
        .post(routes.SetACLController.updateTraderKYCDocumentOrganizationStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.TRADER_ID -> "${%s}".format(Test.TEST_TRADER_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(1)
    }
    .exec(http("Organization_Verify_Trader_GET")
      .get(session=>routes.SetACLController.organizationVerifyTraderForm(session(Test.TEST_SELLER_USERNAME).as[String],session(Test.TEST_ORGANIZATION_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.ORGANIZATION_VERIFY_TRADER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Organization_Verify_Trader_POST")
      .post(routes.SetACLController.organizationVerifyTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_ID-> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ISSUE_ASSET_ACL -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT_ACL -> setACLPrivileges.issueFiat, Form.SEND_ASSET_ACL -> setACLPrivileges.sendAsset, Form.SEND_FIAT_ACL -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET_ACL -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT_ACL -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER_ACL -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER_ACL -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID_ACL -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID_ACL -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID_ACL -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID_ACL -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION_ACL -> setACLPrivileges.negotiation, Form.RELEASE_ASSET_ACL -> setACLPrivileges.releaseAsset,
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ORGANIZATION_PASSWORD)
      ))
      .check(substring("SUCCESS ACL_SET").exists)
    )
    .pause(2)
    .exec(http("Organization_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Organization_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
      asLongAsDuring(session=> session(Test.USER_TYPE).as[String] != "TRADER",Duration.create(80,"seconds")) {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
      }
    }
}

object SignUpAndLoginBuyerAndCreateFaucetRequest{

  val signUpAndLoginBuyerAndCreateFaucetRequest=scenario("createBuyer")
    .feed(BuyerFeeder.buyerFeed)
    .exec(http("Buyer_SignUp_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.SIGN_UP.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Buyer_SignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.USERNAME_AVAILABLE -> true,
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.CONFIRM_PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(css("div:contains(%s)".format(constants.FormField.CONFIRM_NOTE_NEW_KEY_DETAILS.name)).exists)
      // .check(css(s"div:contains('${constants.FormField.CONFIRM_NOTE_NEW_KEY_DETAILS.name}')").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(regex("""blockchainAddress=([^&]*)""").saveAs(Test.TEST_BUYER_ADDRESS))
      .check(regex("""publicKey=([^&]*)""").saveAs(Test.TEST_PUBLIC_KEY))
      .check(regex("""seed=([^"]*)""").saveAs(Test.TEST_SEED))
    )
    .pause(1)
    .exec(http("Note_New_Key_Details_Buyer")
      .post(session=> routes.AccountController.noteNewKeyDetails(session(Test.TEST_BUYER_USERNAME).as[String],session(Test.TEST_BUYER_ADDRESS).as[String],session(Test.TEST_PUBLIC_KEY).as[String],session(Test.TEST_SEED).as[String].replace('+',' ')).url)
      .formParamMap(Map(
        Form.CONFIRM_NOTE_NEW_KEY_DETAILS-> true,
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS SIGNED_UP").exists)
    )
    .pause(3)
    .exec(http("Login_Buyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Buyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(2)
    .feed(CouponFeeder.couponFeed)
    .exec(http("Faucet_Request_Buyer_GET")
      .get(routes.SendCoinController.faucetRequestForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.FAUCET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Faucet_Request_Buyer_POST")
      .post(routes.SendCoinController.faucetRequest().url)
      .formParamMap(Map(
        Form.COUPON -> "${%s}".format(Test.TEST_COUPON),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS COINS_REQUESTED").exists)
    )
    .pause(2)
    .exec(http("Buyer_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Buyer_Logout_Zone_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object ApproveFaucetRequestBuyer{

  val approveFaucetRequestBuyer=scenario("masterLoginMainAndApproveFaucetRequestBuyer")
    .feed(GenesisFeeder.genesisFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("Main_Login_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Main_Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(2)
    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .doIf(session=> session(Test.TEST_REQUEST_ID).as[String] == "0") {
      asLongAsDuring(session=> session(Test.TEST_REQUEST_ID).as[String] =="0",Duration.create(30,"seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_BUYER_USERNAME).as[String])) }
      }
    }
    .exec(http("Approve_Faucet_Request_Buyer_GET")
      .get(session=>routes.SendCoinController.approveFaucetRequestsForm(session(Test.TEST_REQUEST_ID).as[String],session(Test.TEST_BUYER_USERNAME).as[String]).url)
      // .get("/master/approveFaucetRequests?requestID=" + "${%s}".format(Test.TEST_REQUEST_ID) + "&accountID=" + Test.TEST_BUYER_USERNAME)
      .check(css("legend:contains(%s)".format(constants.Form.APPROVE_FAUCET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Approve_Faucet_Request_Buyer_POST")
      .post(routes.SendCoinController.approveFaucetRequests().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS FAUCET_REQUEST_APPROVED").exists)
    )
    .pause(3)
    .exec(http("Main_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Main_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "USER") {
      asLongAsDuring(session=> session(Test.USER_TYPE).as[String] != "USER",Duration.create(80,"seconds")) {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
      }
    }
}

object AddBuyerTraderRequest{

  val addBuyerTraderRequest=scenario("AddBuyerRequest")
    .exec(http("Login_Buyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Buyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .exec(http("Add_Buyer_Form_GET")
      .get(routes.SetACLController.addTraderForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.ADD_TRADER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(NameFeeder.nameFeed)
    .exec(http("Add_Buyer_Form_POST")
      .post(routes.SetACLController.addTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN-> "${%s}".format(Form.CSRF_TOKEN),
        Form.ZONE_ID-> "${%s}".format(Test.TEST_ZONE_ID),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.NAME -> "${%s}".format(Test.TEST_NAME)
      ))
      .check(substring("TRADER_KYC_FILES").exists)
    )
    .pause(2)
    .foreach(setACLControllerTest.traderKYCs,"documentType"){
      feed(ImageFeeder2.imageFeed2)
        .exec(http("Buyer_Trader_Kyc_Upload_"+"${documentType}"+"_FORM")
          .get(session=>routes.SetACLController.userUploadTraderKYCForm(session("documentType").as[String]).url)
          .check(substring("BROWSE").exists)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(2)
        .exec(http("Buyer_Trader_Kyc_Upload_"+"${documentType}")
          .post(session=> routes.SetACLController.userUploadTraderKYC(session("documentType").as[String]).url)
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("Store_Buyer_Trader_Kyc_"+"${documentType}")
            .get(session=>routes.SetACLController.userStoreTraderKYC(session(Test.TEST_FILE_NAME).as[String],session("documentType").as[String]).url)
            .check(substring("TRADER_KYC_FILES").exists)
        )
        .pause(2)
    }
    .exec(http("User_Review_Add_Buyer_Request_FORM")
      .get(routes.SetACLController.userReviewAddTraderRequestForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.REVIEW_ADD_TRADER_ON_COMPLETION.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("User_Review_Add_Buyer_Request")
      .post(routes.SetACLController.userReviewAddTraderRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
      .check(substring("SUCCESS TRADER_ADDED_FOR_VERIFICATION").exists)
    )
    .pause(2)
    .exec(http("Buyer_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Buyer_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object ApproveBuyerTraderACL {

  val approveBuyerTraderACL = scenario("approveBuyerTraderACL")
    .exec(http("Login_Zone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Zone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .doIf(session => session(Test.TEST_TRADER_ID).as[String] == "0") {
      asLongAsDuring(session => session(Test.TEST_TRADER_ID).as[String] == "0",Duration.create(30,"seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_TRADER_ID, setACLControllerTest.getTraderID(session(Test.TEST_BUYER_USERNAME).as[String])) }
      }
    }
    .exec(http("Get_Zone_Pending_Verify_Buyer_Request")
      .get(routes.SetACLController.zoneViewPendingVerifyTraderRequests().url)
      .check(substring("${%s}".format(Test.TEST_TRADER_ID)).exists)
    )
    .pause(2)
    .foreach(setACLControllerTest.traderKYCs,"documentType"){
      exec(http("Zone_Buyer_KYC_update_Status"+"${documentType}")
        .post(routes.SetACLController.updateTraderKYCDocumentZoneStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.TRADER_ID -> "${%s}".format(Test.TEST_TRADER_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(1)
    }
    .exec(http("Zone_Verify_Buyer_GET")
      .get(session=>routes.SetACLController.zoneVerifyTraderForm(session(Test.TEST_BUYER_USERNAME).as[String],session(Test.TEST_ORGANIZATION_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.ZONE_VERIFY_TRADER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Zone_Verify_Buyer_POST")
      .post(routes.SetACLController.zoneVerifyTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_ID-> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ISSUE_ASSET_ACL -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT_ACL -> setACLPrivileges.issueFiat, Form.SEND_ASSET_ACL -> setACLPrivileges.sendAsset, Form.SEND_FIAT_ACL -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET_ACL -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT_ACL -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER_ACL -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER_ACL -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID_ACL -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID_ACL -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID_ACL -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID_ACL -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION_ACL -> setACLPrivileges.negotiation, Form.RELEASE_ASSET_ACL -> setACLPrivileges.releaseAsset,
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD)
      ))
      .check(substring("ALL_KYC_FILES_NOT_VERIFIED").exists)
      .check(status.is(412))
    )
    .pause(2)
    .exec(http("Zone_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Zone_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
    .exec(http("Login_Organization_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Organization_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
        Form.PASSWORD ->"${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .exec(http("Get_Organization_Pending_Verify_Buyer_Request")
      .get(routes.SetACLController.organizationViewPendingVerifyTraderRequests().url)
      .check(substring("${%s}".format(Test.TEST_TRADER_ID)).exists)
    )
    .pause(2)
    .foreach(setACLControllerTest.traderKYCs,"documentType"){
      exec(http("Organization_Buyer_KYC_update_Status"+"${documentType}")
        .post(routes.SetACLController.updateTraderKYCDocumentOrganizationStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.TRADER_ID -> "${%s}".format(Test.TEST_TRADER_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(1)
    }
    .exec(http("Organization_Verify_Buyer_GET")
      .get(session=>routes.SetACLController.organizationVerifyTraderForm(session(Test.TEST_BUYER_USERNAME).as[String],session(Test.TEST_ORGANIZATION_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.ORGANIZATION_VERIFY_TRADER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Organization_Verify_Buyer_POST")
      .post(routes.SetACLController.organizationVerifyTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_ID-> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ISSUE_ASSET_ACL -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT_ACL -> setACLPrivileges.issueFiat, Form.SEND_ASSET_ACL -> setACLPrivileges.sendAsset, Form.SEND_FIAT_ACL -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET_ACL -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT_ACL -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER_ACL -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER_ACL -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID_ACL -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID_ACL -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID_ACL -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID_ACL -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION_ACL -> setACLPrivileges.negotiation, Form.RELEASE_ASSET_ACL -> setACLPrivileges.releaseAsset,
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ORGANIZATION_PASSWORD)
      ))

      .check(substring("SUCCESS ACL_SET").exists)
    )
    .exec{session=>
      println(session)
      session
    }
    .pause(2)
    .exec(http("Organization_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Organization_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
      asLongAsDuring(session=> session(Test.USER_TYPE).as[String] != "TRADER",Duration.create(80,"seconds")) {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
      }
    }
}

object IssueAssetRequestForSeller{

  val issueAssetRequestForSeller=scenario("issueAssetRequestForSeller")
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
      asLongAsDuring(session=> session(Test.USER_TYPE).as[String] != "TRADER",Duration.create(80,"seconds")) {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_SELLER_USERNAME).as[String]))}
      }
    }
    .exec(http("Login_Seller_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Seller_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .feed(AssetTypeFeeder.assetTypeFeed)
    .feed(AssetPriceFeeder.assetPriceFeed)
    .feed(QuantityUnitFeeder.quantityUnitFeed)
    .feed(AssetQuantityFeeder.assetQuantityFeed)
    .exec(http("IssueAssetRequest_GET")
      .get(routes.IssueAssetController.issueAssetDetailForm(None).url)
      .check(css("legend:contains(%s)".format(constants.Form.ISSUE_ASSET_DETAIL.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueAssetRequest_POST")
      .post(routes.IssueAssetController.issueAssetDetail().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "",
        Form.ASSET_TYPE -> "${%s}".format(Test.TEST_ASSET_TYPE),
        Form.QUANTITY_UNIT -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
        Form.TAKER_ADDRESS -> "",
        Form.COMMODITY_NAME -> "AEFRGEAR",
        Form.QUALITY -> "A",
        Form.DELIVERY_TERM -> "FOB",
        Form.TRADE_TYPE -> "POST TRADE",
        Form.PORT_OF_LOADING -> "mumbai",
        Form.PORT_OF_DISCHARGE -> "shanghai",
        Form.SHIPMENT_DATE -> "2019-11-11",
        Form.PHYSICAL_DOCUMENTS_HANDLED_VIA -> "COMDEX",
        Form.COMDEX_PAYMENT_TERMS -> "BOTH_PARTIES",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("UPLOAD OBL").exists)
    )
    .pause(3)
    .exec{session=> session.set(Test.TEST_REQUEST_ID, getRequestIDForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String]))}
    .exec(http("Upload OBL Form")
      .get(routes.FileController.uploadTraderAssetForm("OBL","${TEST_REQUEST_ID}").url)
      .check(substring("BROWSE").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder3.imageFeed3)
    .exec(http("Issue Asset Upload OBL")
      .post(routes.FileController.uploadTraderAsset("OBL").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("Store_AssetDocument_OBL")
        .get(session=> routes.FileController.storeTraderAsset(session(Test.TEST_FILE_NAME).as[String],"OBL",session(Test.TEST_REQUEST_ID).as[String]).url)
        // .get("/traderUpload/storeAssetDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"OBL&issueAssetRequestID="+"${%s}".format(Test.TEST_REQUEST_ID))
        .check(css("legend:contains(%s)".format(constants.Form.ISSUE_ASSET_OBL.legend)).exists)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("Issue_Asset_OBL_POST")
      .post(routes.IssueAssetController.issueAssetOBL().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.BILL_OF_LADING_NUMBER -> "fghfg",
        Form.PORT_OF_LOADING -> "dgdgd",
        Form.SHIPPER_NAME -> "srgsdg",
        Form.SHIPPER_ADDRESS -> "srgsdg",
        Form.NOTIFY_PARTY_NAME ->"srgsdg",
        Form.NOTIFY_PARTY_ADDRESS -> "srgsdg",
        Form.SHIPMENT_DATE ->"2019-11-11",
        Form.DELIVERY_TERM -> "FOB",
        Form.ASSET_QUANTITY -> "123",
        Form.ASSET_PRICE -> "123",
      ))
      .check(substring("UPLOAD INVOICE").exists)
    )
    .pause(2)
    .feed(ImageFeeder2.imageFeed2)
    .exec(http("Issue_Asset_Upload_Invoice")
      .post(routes.FileController.uploadTraderAsset("INVOICE").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("Store_AssetDocument_INVOICE")
        .get(session=> routes.FileController.storeTraderAsset(session(Test.TEST_FILE_NAME).as[String],"INVOICE",session(Test.TEST_REQUEST_ID).as[String]).url)
        //.get("/traderUpload/storeAssetDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"INVOICE&"+"issueAssetRequestID"+"="+"${%s}".format(Test.TEST_REQUEST_ID))
        .check(css("legend:contains(%s)".format(constants.Form.ISSUE_ASSET_INVOICE.legend)).exists)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("issue_Asset_Invoice_POST")
      .post(routes.IssueAssetController.issueAssetInvoice().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        constants.FormField.REQUEST_ID.name -> "${%s}".format(Test.TEST_REQUEST_ID),
        constants.FormField.INVOICE_NUMBER.name ->"sdfgsdvsrdvsrev",
        constants.FormField.INVOICE_DATE.name -> "2019-11-11",
      ))
      .check(substring("UPLOAD CONTRACT").exists)
      .check(substring("UPLOAD PACKING_LIST").exists)
      .check(substring("UPLOAD COO").exists)
      .check(substring("UPLOAD COA").exists)
      .check(substring("UPLOAD OTHER").exists)
    )
    .pause(2)
    .foreach(issueAssetControllerTest.issueAssetDocumentType,"documentType"){
      feed(ImageFeeder.imageFeed)
        .exec(http("Issue_Asset_Upload_"+"${documentType}"+"_FORM")
          .get(session=> routes.FileController.uploadTraderAssetForm(session("documentType").as[String],session(Test.TEST_REQUEST_ID).as[String]).url)
          .check(substring("BROWSE").exists)
          //.get("/traderUpload/assetForm?documentType="+"${documentType}"+"&issueAssetRequestID="+"${%s}".format(Test.TEST_REQUEST_ID))
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(2)
        .exec(http("Issue_Asset_Upload_"+"${documentType}")
          .post(session=> routes.FileController.uploadTraderAsset(session("documentType").as[String]).url)
          .formParamMap(Map(
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
            Form.RESUMABLE_CHUNK_NUMBER -> "1",
            Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
            Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
            Form.RESUMABLE_IDENTIFIER -> "document",
            Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
          .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
            .transferEncoding("binary")).asMultipartForm)
        .exec(
          http("Store_Asset_Document_"+"${documentType}")
            .get(session=> routes.FileController.storeTraderAsset(session(Test.TEST_FILE_NAME).as[String],session("documentType").as[String],session(Test.TEST_REQUEST_ID).as[String]).url)

          //.get("/traderUpload/storeAssetDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType="+"${documentType}"+"&issueAssetRequestID="+"${%s}".format(Test.TEST_REQUEST_ID))
        )
        .pause(2)
    }
    .pause(1)
    .exec(http("Issue_Asset_Request_Form")
      .get(session=>routes.IssueAssetController.issueAssetRequestForm(session(Test.TEST_REQUEST_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.ISSUE_ASSET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("Issue_Asset_Request")
      .post(routes.IssueAssetController.issueAssetRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
      ))
      .check(substring("SUCCESS ISSUE_ASSET_REQUEST_SENT").exists)
    )
    .pause(2)
    .exec(http("Seller_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Seller_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object ApproveIssueAsset{

  val approveIssueAsset=scenario("approveIssueAsset")
    .exec(http("Login_Zone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Zone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .exec(http("Get_Pending_Issue_Asset_Request")
      .get(routes.IssueAssetController.viewPendingIssueAssetRequests().url)
      .check(substring("${%s}".format(Test.TEST_SELLER_USERNAME)).exists)
    )
    .pause(2)
    .foreach(allIssueAssetDocumentType,"documentType"){
      exec(http("Update_Asset_Document_Status_"+"${documentType}")
        .post(routes.IssueAssetController.updateAssetDocumentStatus().url)
        .formParamMap(Map(
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
          Form.FILE_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
          Form.DOCUMENT_TYPE -> "${documentType}",
          Form.STATUS -> true
        ))
      )
        .pause(1)
    }
    .exec(session=> session.set(Test.TEST_DOCUMENT_HASH,getDocumentHashForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_ASSET_TYPE,getAssetTypeForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_ASSET_PRICE,getAssetPriceForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_QUANTITY_UNIT,getQuantityUnitForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_ASSET_QUANTITY,getAssetQuantityForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])))
    .exec(http("Issue_Asset_Form")
      .get(session=> routes.IssueAssetController.issueAssetForm(session(Test.TEST_REQUEST_ID).as[String],session(Test.TEST_SELLER_USERNAME).as[String],session(Test.TEST_DOCUMENT_HASH).as[String],session(Test.TEST_ASSET_TYPE).as[String],session(Test.TEST_ASSET_PRICE).as[Int],session(Test.TEST_QUANTITY_UNIT).as[String],session(Test.TEST_ASSET_QUANTITY).as[Int],None).url)
      .check(css("legend:contains(%s)".format(constants.Form.ISSUE_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Issue_Asset")
      .post(routes.IssueAssetController.issueAsset().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID ->"${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.DOCUMENT_HASH -> "${%s}".format(Test.TEST_DOCUMENT_HASH),
        Form.ASSET_TYPE -> "${%s}".format(Test.TEST_ASSET_TYPE),
        Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
        Form.QUANTITY_UNIT -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        Form.TAKER_ADDRESS-> "",
        Form.GAS->"${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD)
      ))
      .check(substring("SUCCESS ASSET_ISSUED").exists)
    )
    .pause(2)
    .exec(http("Zone_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Zone_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object IssueFiatRequestBuyer{

  val issueFiatRequest=scenario("IssueFiatRequest")
    .exec{session => session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "TRADER") {
      asLongAsDuring(session=> session(Test.USER_TYPE).as[String] != "TRADER",Duration.create(80,"seconds")) {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, sendCoinControllerTest.getUserType(session(Test.TEST_BUYER_USERNAME).as[String])) }
      }
    }
    .exec(http("Login_Buyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Buyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .feed(TransactionIDFeeder.transactionIDFeed)
    .feed(TransactionAmountFeeder.transactionAmountFeed)
    .exec(http("Issue_Fiat_Request_GET")
      .get(routes.IssueFiatController.issueFiatRequestForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.ISSUE_FIAT_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Issue_Fiat_Request_POST")
      .post(routes.IssueFiatController.issueFiatRequest().url)
      .formParamMap(Map(
        Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
        Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ISSUE_FIAT_REQUEST_SENT").exists)
    )
    .pause(2)
    .exec(http("Buyer_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Buyer_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object IssueFiat{
  val issueFiat=scenario("issueFiat")
    .exec(http("Login_Zone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Zone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .feed(GasFeeder.gasFeed)
    .exec(session=> session.set(Test.TEST_REQUEST_ID,getRequestIDForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])).set(Test.TEST_TRANSACTION_ID,getTransactionIDForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])).set(Test.TEST_TRANSACTION_AMOUNT,getTransactionAmountForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])))
    .exec{session=> println(session)
      session}
    .exec(http("Issue_Fiat_GET")
      .get(session=> routes.IssueFiatController.issueFiatForm(session(Test.TEST_REQUEST_ID).as[String],session(Test.TEST_BUYER_USERNAME).as[String],session(Test.TEST_TRANSACTION_ID).as[String],session(Test.TEST_TRANSACTION_AMOUNT).as[Int]).url)
      .check(css("legend:contains(%s)".format(constants.Form.ISSUE_FIAT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Issue_Fiat_POST")
      .post(routes.IssueFiatController.issueFiat().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
        Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS FIAT_ISSUED").exists)
    )
    .pause(2)
    .exec(http("Zone_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Zone_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object ChangeBuyerBid{

  val changeBuyerBid=scenario("changeBuyerBid")
    .exec(http("Login_Buyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Buyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(2)
    .feed(BuyerBidFeeder.buyerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(GasFeeder.gasFeed)

    .exec{session=>session.set(Test.TEST_PEG_HASH,issueAssetControllerTest.getPegHashByOwnerAddress(session(Test.TEST_SELLER_USERNAME).as[String]))}
    .doIf(session=> session(Test.TEST_PEG_HASH).as[String] == "0") {
      asLongAsDuring(session=> session(Test.TEST_PEG_HASH).as[String] == "0",Duration.create(30,"seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_PEG_HASH,issueAssetControllerTest.getPegHashByOwnerAddress(session(Test.TEST_SELLER_USERNAME).as[String]))}
      }
    }
    .exec{session=> println(session)
      session}
    .exec(http("Change_Buyer_Bid_GET")
      .get(session=>routes.ChangeBuyerBidController.changeBuyerBidForm(session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.CHANGE_BUYER_BID.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.REQUEST_ID), "value").saveAs(Form.REQUEST_ID))
    )
    .pause(2)
    .exec(http("Change_Buyer_Bid_POST")
      .post(routes.ChangeBuyerBidController.changeBuyerBid().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID-> "${%s}".format(Form.REQUEST_ID),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.BID -> "${%s}".format(Test.TEST_BUYER_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD)))
      .check(substring("SUCCESS BUYER_BID_CHANGED").exists)
    )
    .pause(2)
    .exec(http("Buyer_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Buyer_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object ChangeSellerBid{

  val changeSellerBid=scenario("changeSellerBid")
    .feed(SellerBidFeeder.sellerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("Login_Seller_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Seller_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .exec{session=> println(session)
      session}
    .exec(http("Change_Seller_Bid_GET")
      .get(session=>routes.ChangeSellerBidController.changeSellerBidForm(session(Test.TEST_BUYER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.CHANGE_SELLER_BID.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.REQUEST_ID), "value").saveAs(Form.REQUEST_ID))
    )
    .pause(2)
    .exec(http("Change_Seller_Bid_POST")
      .post(routes.ChangeSellerBidController.changeSellerBid().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID -> "${%s}".format(Form.REQUEST_ID),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.BID -> "${%s}".format(Test.TEST_SELLER_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD)))
      .check(substring("SUCCESS SELLER_BID_CHANGED").exists)
    )
    .pause(2)
    .exec(http("Seller_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Seller_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object ConfirmBuyerBid{
  val confirmBuyerBid=scenario("confirmBuyerBid")
    .exec(http("Login_Buyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Buyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(2)
    .exec(http("Confirm_Buyer_Bid_Detail_GET")
      .get(session=>routes.ConfirmBuyerBidController.confirmBuyerBidDetailForm(session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String],950).url)
      .check(css("legend:contains(%s)".format(constants.Form.CONFIRM_BUYER_BID_DETAIL.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.REQUEST_ID), "value").saveAs(Form.REQUEST_ID))
    )
    .pause(2)
    .exec(http("Confirm_Buyer_Bid_Detail_POST")
      .post(routes.ConfirmBuyerBidController.confirmBuyerBidDetail().url)
      .formParamMap(Map(
        Form.REQUEST_ID-> "${%s}".format(Form.REQUEST_ID),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.BID -> "950",
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("UPLOAD BUYER_CONTRACT").exists)
    )
    .pause(2)
    .feed(ImageFeeder.imageFeed)
    .exec(http("Trader_Upload_BuyerContract_Negotiation_Form")
      .get(session=> routes.FileController.uploadTraderNegotiationForm("BUYER_CONTRACT",session(Form.REQUEST_ID).as[String]).url)
      .check(substring("BROWSE").exists)
      //   .get("/traderUpload/negotiationForm?documentType=BUYER_CONTRACT&negotiationRequestID="+ "${%s}".format(Form.REQUEST_ID))
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("Trader_Upload_Buyer_Contract")
      .post(routes.FileController.uploadTraderNegotiation("BUYER_CONTRACT").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("Store_Negotiation_Buyer_Contract_Hash")
        .get(session=> routes.FileController.storeTraderNegotiation(session(Test.TEST_FILE_NAME).as[String],"BUYER_CONTRACT",session(Form.REQUEST_ID).as[String]).url)
    )
    .pause(2)
    .exec(http("Confirm_Buyer_Bid_Form_GET")

      .get(session=> routes.ConfirmBuyerBidController.confirmBuyerBidForm(session(Form.REQUEST_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.CONFIRM_BUYER_BID.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("Confirm_Buyer_Bid_POST")
      .post(routes.ConfirmBuyerBidController.confirmBuyerBid().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID-> "${%s}".format(Form.REQUEST_ID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD)
      ))
      .check(substring("SUCCESS BUYER_BID_CONFIRMED").exists)
    )
    .pause(3)
    .exec(http("Buyer_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Buyer_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object ConfirmSellerBid{
  val confirmSellerBid=scenario("confirmSellerBid")
    .exec(http("Login_Seller_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Seller_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .feed(SellerBidFeeder.sellerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("Confirm_Seller_Bid_Detail_GET")
      //.get("/master/confirmSellerBidDetail?buyerAddress="+"${%s}".format(Test.TEST_BUYER_ADDRESS)+"&pegHash="+"${%s}".format(Test.TEST_PEG_HASH)+"&bid="+"950")
      .get(session=> routes.ConfirmSellerBidController.confirmSellerBidDetailForm(session(Test.TEST_BUYER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String],950).url)
      .check(css("legend:contains(%s)".format(constants.Form.CONFIRM_SELLER_BID_DETAIL.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.REQUEST_ID), "value").saveAs(Form.REQUEST_ID))
    )
    .pause(2)
    .exec(http("Confirm_Seller_Bid_Detail_POST")
      .post(routes.ConfirmSellerBidController.confirmSellerBidDetail().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Form.REQUEST_ID),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.BID -> "950",
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("UPLOAD SELLER_CONTRACT").exists)
    )
    .pause(2)
    .feed(ImageFeeder2.imageFeed2)
    .exec(http("Trader_Upload_Seller_Contract_Negotiation_Form")
      .get(session=>routes.FileController.uploadTraderNegotiationForm("SELLER_CONTRACT",session(Form.REQUEST_ID).as[String]).url)
      .check(substring("BROWSE").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("Trader_Upload_Seller_Contract")
      .post(routes.FileController.uploadTraderNegotiation("SELLER_CONTRACT").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("Store_Negotiation_Seller_Contract_Hash")
        .get(session=> routes.FileController.storeTraderNegotiation(session(Test.TEST_FILE_NAME).as[String],"SELLER_CONTRACT",session(Form.REQUEST_ID).as[String]).url)
    )
    .pause(2)
    .exec(http("Confirm_Seller_Bid_Form_GET")
      // .get("/master/confirmSellerBid?requestID="+ "${%s}".format(Form.REQUEST_ID))
      .get(session=> routes.ConfirmSellerBidController.confirmSellerBidForm(session(Form.REQUEST_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.CONFIRM_SELLER_BID.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("Confirm_Seller_Bid_POST")
      .post(routes.ConfirmSellerBidController.confirmSellerBid().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.REQUEST_ID-> "${%s}".format(Form.REQUEST_ID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD)
      ))
      .check(substring("SUCCESS SELLER_BID_CONFIRMED").exists)
    )
    .pause(2)
    .exec(http("Seller_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Seller_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object SendFiat{

  val sendFiat=scenario("sendFiat")
    .exec(http("Login_Buyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Buyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .feed(AmountFeeder.amountFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("Send_Fiat_Form_GET")
      .get(session=>routes.SendFiatController.sendFiatForm(session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String],session(Test.TEST_AMOUNT).as[Int]).url)
      .check(css("legend:contains(%s)".format(constants.Form.SEND_FIAT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Send_Fiat_POST")
      .post(routes.SendFiatController.sendFiat().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.AMOUNT -> "${%s}".format(Test.TEST_AMOUNT),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)
      ))
      .check(substring("SUCCESS FIAT_SENT").exists)
    )
    .pause(3)
    .exec(http("Buyer_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Buyer_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)

}

object ReleaseAsset{

  val releaseAsset=scenario("releaseAsset")
    .exec(http("Login_Zone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Zone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(2)
    .exec(http("Release_Asset_Form_GET")
      .get(session=> routes.ReleaseAssetController.releaseAssetForm(session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.RELEASE_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Release_Asset_POST")
      .post(routes.ReleaseAssetController.releaseAsset().url)
      .formParamMap(Map(
        Form.BLOCKCHAIN_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ASSET_RELEASED").exists)
    )
    .pause(3)
    .exec(http("Zone_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Zone_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object SendAsset{

  val sendAsset=scenario("sendAsset")
    .exec(http("Login_Buyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Buyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .exec(http("Send_Asset_GET")
      .get(session=> routes.SendAssetController.sendAssetForm(session(Test.TEST_BUYER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.SEND_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Send_Asset_POST")
      .post(routes.SendAssetController.sendAsset().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ASSET_SENT").exists)
    )
    .pause(3)
    .exec(http("Seller_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Seller_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object BuyerAndSellerExecuteOrder{

  val buyerAndSellerExecuteOrder=scenario("buyerAndSellerExecuteOrder")
    .exec(http("Login_Zone_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Login_Zone_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME ->  "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .exec(session=> session.set(Test.TEST_NEGOTIATION_REQUEST_ID,confirmBuyerBidControllerTest.getNegotiationRequestIDFromSellerAccountID(session(Test.TEST_SELLER_USERNAME).as[String])))
    .exec(http("Moderated_Buyer_Execute_Order_Document_Upload_GetForm")
      .get(session=>routes.BuyerExecuteOrderController.moderatedBuyerExecuteOrderDocument(session(Test.TEST_BUYER_ADDRESS).as[String],session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(substring("FIAT_PROOF").exists)
    )
    .pause(1)
    .exec(http("Zone_Upload_Negotiation_FIAT_PROOF")
      .get(session=> routes.FileController.uploadZoneNegotiationForm("FIAT_PROOF",session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
      .check(substring("BROWSE").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder3.imageFeed3)
    .feed(GasFeeder.gasFeed)
    .exec(http("Zone_Upload_Negotiation_FIAT_PROOF")
      .post(routes.FileController.uploadZoneNegotiation("FIAT_PROOF").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm
    )
    .exec(
      http("Store_Negotiation_Document_FIAT_Proof")
        .get(session=>routes.FileController.storeZoneNegotiation(session(Test.TEST_FILE_NAME).as[String],"FIAT_PROOF",session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
    )
    .pause(2)
    .exec(http("Moderated_Buyer_Execute_Form_Get")
      .get(session=> routes.BuyerExecuteOrderController.moderatedBuyerExecuteOrderForm(session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.MODERATED_BUYER_EXECUTE_ORDER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.FIAT_PROOF_HASH), "value").saveAs(Form.FIAT_PROOF_HASH))
    )
    .pause(2)
    .exec(http("Moderated_Buyer_Execute_POST")
      .post(routes.BuyerExecuteOrderController.moderatedBuyerExecuteOrder().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.FIAT_PROOF_HASH -> "${%s}".format(Form.FIAT_PROOF_HASH),
        Form.PEG_HASH ->  "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD)
      ))
      .check(substring("SUCCESS BUYER_ORDER_EXECUTED").exists)
    )
    .pause(3)
    .exec(http("Moderated_Seller_Execute_Order_Document_Upload_GetForm")
      .get(session=>routes.SellerExecuteOrderController.moderatedSellerExecuteOrderDocument(session(Test.TEST_BUYER_ADDRESS).as[String],session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(substring("AWB_PROOF").exists)
    )
    .pause(1)
    .exec(http("Zone_Upload_Negotiation_Form_AWB_PROOF")
      .get(session=> routes.FileController.uploadZoneNegotiationForm("AWB_PROOF",session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
      .check(substring("BROWSE").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder4.imageFeed4)
    .feed(GasFeeder.gasFeed)
    .exec(http("Zone_Upload_Negotiation_AWB_PROOF")
      .post(routes.FileController.uploadZoneNegotiation("AWB_PROOF").url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file",Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm
    )
    .exec(
      http("Store_Negotiation_Document_AWBProof")
        .get(session=>routes.FileController.storeZoneNegotiation(session(Test.TEST_FILE_NAME).as[String],"AWB_PROOF",session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
      // .get("/zoneUpload/storeNegotiationDocument?name="+"${%s}".format(Test.TEST_FILE_NAME)+"&documentType=AWB_PROOF&negotiationRequestID="+"${%s}".format(Test.TEST_NEGOTIATION_REQUEST_ID))
    )
    .pause(2)
    .exec(http("Moderated_Seller_Execute_Form_Get")
      .get(session=> routes.SellerExecuteOrderController.moderatedSellerExecuteOrderForm(session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.MODERATED_SELLER_EXECUTE_ORDER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.AWB_PROOF_HASH), "value").saveAs(Form.AWB_PROOF_HASH))
    )
    .pause(2)
    .exec(http("Moderated_Seller_Execute_POST")
      .post(routes.SellerExecuteOrderController.moderatedSellerExecuteOrder().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.AWB_PROOF_HASH -> "${%s}".format(Form.AWB_PROOF_HASH),
        Form.PEG_HASH ->  "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD)
      ))
      .check(substring("SUCCESS SELLER_ORDER_EXECUTED").exists)
    )
    .pause(3)
    .exec(http("Zone_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Zone_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)

}

object SetBuyerFeedback{

  val setBuyerFeedback=scenario("setBuyerFeedback")
    .exec(http("Login_Buyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Login_Buyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .feed(RatingFeeder.ratingFeed)
    .exec(http("Set_Buyer_Feedback_GET")
      .get(session=> routes.SetBuyerFeedbackController.setBuyerFeedbackForm(session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.SET_BUYER_FEEDBACK.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Set_Buyer_Feedback_POST")
      .post(routes.SetBuyerFeedbackController.setBuyerFeedback().url)
      .formParamMap(Map(
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.RATING -> "${%s}".format(Test.TEST_RATING),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS BUYER_FEEDBACK_SET").exists)
    )
    .pause(2)
    .exec(http("Buyer_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Buyer_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object SetSellerFeedback{

  val setSellerFeedback=scenario("setSellerFeedback")
    .exec(http("Login_Seller_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Login_Seller_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .feed(RatingFeeder.ratingFeed)
    .exec(http("Set_Seller_Feedback_Form_GET")
      .get(session=> routes.SetSellerFeedbackController.setSellerFeedbackForm(session(Test.TEST_BUYER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.SET_SELLER_FEEDBACK.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Set_Seller_Feedback_POST")
      .post(routes.SetSellerFeedbackController.setSellerFeedback().url)
      .formParamMap(Map(
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.RATING -> "${%s}".format(Test.TEST_RATING),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS SELLER_FEEDBACK_SET").exists)
    )
    .pause(2)
    .exec(http("Seller_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Seller_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}


object RedeemAsset{

  val redeemAsset=scenario("redeemAsset")
    .exec(http("Login_Buyer_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Login_Buyer_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(3)
    .exec(http("Redeem_Asset_GET")
      .get(session=> routes.RedeemAssetController.redeemAssetForm(session(Test.TEST_BUYER_USERNAME).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.REDEEM_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Redeem_Asset_POST")
      .post(routes.RedeemAssetController.redeemAsset().url)
      .formParamMap(Map(
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ASSET_REDEEMED").exists)
    )
    .pause(3)
    .exec(http("Buyer_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Buyer_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}

object RedeemFiat{

  val redeemFiat=scenario("redeemFiat")
    .feed(RedeemAmountFeeder.redeemAmountFeed)
    .exec(http("Login_Seller_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .pause(2)
    .exec(http("Login_Seller_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("WARNING UPDATE_CONTACT_DETAILS").exists)
    )
    .pause(2)
    .exec(http("Redeem_Fiat_GET")
      .get(session=> routes.RedeemFiatController.redeemFiatForm(session(Test.TEST_SELLER_USERNAME).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.REDEEM_FIAT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Redeem_Fiat_POST")
      .post(routes.RedeemFiatController.redeemFiat().url)
      .formParamMap(Map(
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.REDEEM_AMOUNT -> "${%s}".format(Test.TEST_REDEEM_AMOUNT),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS FIAT_REDEEMED").exists)
    )
    .pause(2)
    .exec(http("Seller_Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGOUT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(1)
    .exec(http("Seller_Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.RECEIVE_NOTIFICATIONS -> false
      ))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(3)
}


*/
