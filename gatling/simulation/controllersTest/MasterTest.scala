package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.addOrganizationControllerTest._
import controllersTest.addZoneControllerTest._
import controllersTest.issueAssetControllerTest.getRequestIDForIssueAsset
import controllersTest.issueFiatControllerTest.getRequestIDForIssueFiat
import controllersTest.sendCoinControllerTest.getRequestIDForFaucetRequest
import controllersTest.setACLControllerTest._
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class MasterTest extends Simulation {

  setUp(
    //    masterTest.masterTestSignUp.inject(atOnceUsers(8)),
    //    masterTest.masterLoginAndRequestCoin.inject(nothingFor(20), atOnceUsers(8)),
    //    masterTest.masterLoginMainAndApproveFaucetRequest.inject(nothingFor(40), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1)),
    //    masterTest.masterZoneLogin.inject(nothingFor(150), atOnceUsers(2)),
    //    masterTest.masterLoginMainAndApproveZone.inject(nothingFor(210), atOnceUsers(1), nothingFor(15), atOnceUsers(1)),
    //    masterTest.masterOrganizationLogin.inject(nothingFor(240), atOnceUsers(2)),
    //    masterTest.masterLoginZoneAndApproveOrganization.inject(nothingFor(265), atOnceUsers(1), nothingFor(15), atOnceUsers(1)),
    //    masterTest.masterLoginZoneAndSetACL.inject(nothingFor(295), atOnceUsers(1), nothingFor(20), atOnceUsers(1)),
    //    masterTest.masterSellerLoginAndIssueAssetRequest.inject(nothingFor(340), atOnceUsers(2)),
    //    masterTest.masterLoginZoneAndIssueAsset.inject(nothingFor(365), atOnceUsers(2)),
    //    masterTest.masterBuyerLoginAndIssueFiatRequest.inject(nothingFor(390), atOnceUsers(2)),
    //    masterTest.masterLoginZoneAndIssueFiat.inject(nothingFor(415), atOnceUsers(2)),
    masterTest.masterTestSignUp.inject(atOnceUsers(4)),
    masterTest.masterLoginAndRequestCoin.inject(nothingFor(15), atOnceUsers(4)),
    masterTest.masterLoginMainAndApproveFaucetRequest.inject(nothingFor(25), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1)),
    masterTest.masterZoneLogin.inject(nothingFor(100), atOnceUsers(1)),
    masterTest.masterLoginMainAndApproveZone.inject(nothingFor(120), atOnceUsers(1)),
    masterTest.masterOrganizationLogin.inject(nothingFor(150), atOnceUsers(1)),
    masterTest.masterLoginZoneAndApproveOrganization.inject(nothingFor(170), atOnceUsers(1)),
    masterTest.masterLoginZoneAndSetACL.inject(nothingFor(200), atOnceUsers(1)),
    masterTest.masterSellerLoginAndIssueAssetRequest.inject(nothingFor(230), atOnceUsers(1)),
    masterTest.masterLoginZoneAndIssueAsset.inject(nothingFor(250), atOnceUsers(1)),
    masterTest.masterBuyerLoginAndIssueFiatRequest.inject(nothingFor(270), atOnceUsers(1)),
    masterTest.masterLoginZoneAndIssueFiat.inject(nothingFor(285), atOnceUsers(1))
  ).protocols(http.baseUrl(Test.BASE_URL))
}

object masterTest {

  val masterTestSignUp: ScenarioBuilder = scenario("masterTestSignUp")
    .exec(controllersTest.signUpControllerTest.signUpScenario)

  val masterLoginAndRequestCoin: ScenarioBuilder = scenario("masterLoginAndRequestCoin")
    .feed(UsernameFeeder.usernameFeed)
    .feed(PasswordFeeder.passwordFeed)
    .exec(controllersTest.loginControllerTest.loginWithoutSignUpScenario)
    .pause(2)
    .exec(controllersTest.sendCoinControllerTest.requestCoinScenario)

  val masterLoginMainAndApproveFaucetRequest: ScenarioBuilder = scenario("masterLoginMainAndApproveFaucetRequest")
    .feed(GenesisFeeder.genesisFeed)
    .feed(AccountIDFeeder.accountIDFeed)
    .feed(RequestIDFeeder.requestIDFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("MainLogin_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("MainLogin_POST")
      .post(routes.LoginController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_ACCOUNT_ID).as[String])) }
    .exec { session => println(session); session }
    .exec(http("ApproveFaucetRequest_GET")
      .get(routes.SendCoinController.approveFaucetRequestsForm("${%s}".format(Test.TEST_REQUEST_ID), "${%s}".format(Test.TEST_ACCOUNT_ID)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ApproveFaucetRequest_POST")
      .post(routes.SendCoinController.approveFaucetRequests().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ACCOUNT_ID),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

  val masterZoneLogin: ScenarioBuilder = scenario("masterZoneLogin")
    .feed(ZoneLoginFeeder.zoneLoginFeed)
    .exec(http("ZoneLogin_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("ZoneLogin_POST")
      .post(routes.LoginController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
        Form.NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
    .exec(controllersTest.addZoneControllerTest.addZoneScenario)

  val masterLoginMainAndApproveZone: ScenarioBuilder = scenario("masterLoginMainAndApproveZone")
    .feed(GenesisFeeder.genesisFeed)
    .feed(ZoneLoginFeeder.zoneLoginFeed)
    .feed(ZoneIDFeeder.zoneIDFeed)
    .exec(http("MainLogin_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("MainLogin_POST")
      .post(routes.LoginController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .exec(http("VerifyZone_GET")
      .get(routes.AddZoneController.verifyZoneForm("${%s}".format(Test.TEST_ZONE_ID)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec { session => println(session); session }
    .exec(http("VerifyZone_POST")
      .post(routes.AddZoneController.verifyZone().url)
      .formParamMap(Map(
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val masterOrganizationLogin: ScenarioBuilder = scenario("masterOrganizationLogin")
    .feed(OrganizationLoginFeeder.organizationLoginFeed)
    .feed(ZoneLoginFeeder.zoneLoginFeed)
    .exec { session => println(session); session }
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .doIfOrElse(session => getZoneStatus(session(Test.TEST_ZONE_USERNAME).as[String])) {
      exec { session => println("InsideLogin" + session); session }
        .exec(http("OrganizationLogin_GET")
          .get(routes.LoginController.loginForm().url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .exec(http("OrganizationLogin_POST")
          .post(routes.LoginController.login().url)
          .formParamMap(Map(
            Form.USERNAME -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
            Form.PASSWORD -> "${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
            Form.NOTIFICATION_TOKEN -> "",
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(5).exec(controllersTest.addOrganizationControllerTest.addOrganizationScenario)
    } {
      exec { session => println("OutsideLogin" + session); session }
    }

  val masterLoginZoneAndApproveOrganization: ScenarioBuilder = scenario("masterLoginZoneAndApproveOrganization")
    .feed(ZoneLoginFeeder.zoneLoginFeed)
    .feed(OrganizationLoginFeeder.organizationLoginFeed)
    .feed(ZoneIDFeeder.zoneIDFeed)
    .feed(OrganizationIDFeeder.organizationIDFeed)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .doIfOrElse(session => getZoneStatus(session(Test.TEST_ZONE_USERNAME).as[String])) {
      exec { session => println("InsideApprove" + session); session }
        .exec(http("VerifiedZoneLogin_GET")
          .get(routes.LoginController.loginForm().url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .exec(http("VerifiedZoneLogin_POST")
          .post(routes.LoginController.login().url)
          .formParamMap(Map(
            Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
            Form.NOTIFICATION_TOKEN -> "",
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(2)
        .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
        .exec(http("VerifyOrganization_GET")
          .get(routes.AddOrganizationController.verifyOrganizationForm("${%s}".format(Test.TEST_ORGANIZATION_ID), "${%s}".format(Test.TEST_ZONE_ID)).url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .pause(2)
        .exec { session => println("InsideVerification" + session); session }
        .exec(http("VerifyOrganization_POST")
          .post(routes.AddOrganizationController.verifyOrganization().url)
          .formParamMap(Map(
            Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
            Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(5)
    } {
      exec { session => println("OutsideApprove" + session); session }
    }

  val masterLoginZoneAndSetACL: ScenarioBuilder = scenario("masterLoginZoneAndSetACL")
    .feed(ZoneLoginFeeder.zoneLoginFeed)
    .feed(OrganizationLoginFeeder.organizationLoginFeed)
    .feed(ZoneIDFeeder.zoneIDFeed)
    .feed(OrganizationIDFeeder.organizationIDFeed)
    .feed(SellerLoginFeeder.sellerLoginFeeder)
    .feed(BuyerLoginFeeder.buyerLoginFeeder)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .doIfOrElse(session => getZoneStatus(session(Test.TEST_ZONE_USERNAME).as[String])) {
      exec(http("VerifiedZoneLogin_GET")
        .get(routes.LoginController.loginForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .exec(http("VerifiedZoneLogin_POST")
          .post(routes.LoginController.login().url)
          .formParamMap(Map(
            Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
            Form.NOTIFICATION_TOKEN -> "",
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(2)
        .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
        .exec(http("SetACLSeller_GET")
          .get(routes.SetACLController.setACLForm().url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .pause(2)
        .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getAccountAddressByUsername(session(Test.TEST_SELLER_USERNAME).as[String])) }
        .exec(http("SetACLSeller_POST")
          .post(routes.SetACLController.setACL().url)
          .formParamMap(Map(
            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
            Form.ACL_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
            Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
            Form.ISSUE_ASSET -> true, Form.ISSUE_ASSET -> true, Form.ISSUE_FIAT -> true, Form.SEND_ASSET -> true, Form.SEND_FIAT -> true, Form.REDEEM_ASSET -> true, Form.REDEEM_FIAT -> true, Form.SELLER_EXECUTE_ORDER -> true, Form.BUYER_EXECUTE_ORDER -> true, Form.CHANGE_BUYER_BID -> true, Form.CHANGE_SELLER_BID -> true, Form.CONFIRM_BUYER_BID -> true, Form.CONFIRM_SELLER_BID -> true, Form.NEGOTIATION -> true, Form.RELEASE_ASSET -> true,
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(5)
        .exec(http("SetACLBuyer_GET")
          .get(routes.SetACLController.setACLForm().url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .pause(2)
        .exec { session => session.set(Test.TEST_BUYER_ADDRESS, getAccountAddressByUsername(session(Test.TEST_BUYER_USERNAME).as[String])) }
        .exec(http("SetACLBuyer_POST")
          .post(routes.SetACLController.setACL().url)
          .formParamMap(Map(
            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
            Form.ACL_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
            Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
            Form.ISSUE_ASSET -> true, Form.ISSUE_ASSET -> true, Form.ISSUE_FIAT -> true, Form.SEND_ASSET -> true, Form.SEND_FIAT -> true, Form.REDEEM_ASSET -> true, Form.REDEEM_FIAT -> true, Form.SELLER_EXECUTE_ORDER -> true, Form.BUYER_EXECUTE_ORDER -> true, Form.CHANGE_BUYER_BID -> true, Form.CHANGE_SELLER_BID -> true, Form.CONFIRM_BUYER_BID -> true, Form.CONFIRM_SELLER_BID -> true, Form.NEGOTIATION -> true, Form.RELEASE_ASSET -> true,
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .exec { session => println("InsideSetACL" + ",ZONE_USERNAME--" + session(Test.TEST_ZONE_USERNAME).as[String] + ",SELLER_USERNAME--" + session(Test.TEST_SELLER_USERNAME).as[String] + ",BUYER_USERNAME--" + session(Test.TEST_BUYER_USERNAME).as[String] + ",ORGANIZATION_USERNAME--" + session(Test.TEST_ORGANIZATION_USERNAME).as[String]); session }
        .pause(5)
    } {
      exec { session => println("OutsideApprove" + session); session }
    }

  val masterSellerLoginAndIssueAssetRequest: ScenarioBuilder = scenario("masterSellerLoginAndIssueAssetRequest")
    .feed(SellerLoginFeeder.sellerLoginFeeder)
    .exec(http("SellerLogin_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("SellerLogin_POST")
      .post(routes.LoginController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec(controllersTest.issueAssetControllerTest.issueAssetRequestScenario)
    .pause(5)

  val masterLoginZoneAndIssueAsset: ScenarioBuilder = scenario("masterLoginZoneAndIssueAsset")
    .feed(ZoneLoginFeeder.zoneLoginFeed.queue)
    .feed(ZoneIDFeeder.zoneIDFeed)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .doIfOrElse(session => getZoneStatus(session(Test.TEST_ZONE_USERNAME).as[String])) {
      exec(http("VerifiedZoneLogin_GET")
        .get(routes.LoginController.loginForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .exec(http("VerifiedZoneLogin_POST")
          .post(routes.LoginController.login().url)
          .formParamMap(Map(
            Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
            Form.NOTIFICATION_TOKEN -> "",
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(2)
        .feed(SellerLoginFeeder.sellerLoginFeeder)
        .feed(BuyerLoginFeeder.buyerLoginFeeder)
        .feed(DocumentHashFeeder.documentHashFeed)
        .feed(AssetTypeFeeder.assetTypeFeed)
        .feed(AssetPriceFeeder.assetPriceFeed)
        .feed(QuantityUnitFeeder.quantityUnitFeed)
        .feed(AssetQuantityFeeder.assetQuantityFeed)
        .feed(PasswordFeeder.passwordFeed)
        .feed(GasFeeder.gasFeed)
        .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])) }
        .exec { session => session.set(Test.TEST_ACCOUNT_ID, session(Test.TEST_SELLER_USERNAME).as[String]) }
        .exec { session => println("InsideIssueAsset" + ",ZONE_USERNAME--" + session(Test.TEST_ZONE_USERNAME).as[String] + ",SELLER_USERNAME--" + session(Test.TEST_SELLER_USERNAME).as[String] + ",BUYER_USERNAME--" + session(Test.TEST_BUYER_USERNAME).as[String]); session }
        .exec(http("IssueAsset_GET")
          .get(routes.IssueAssetController.issueAssetForm("${%s}".format(Test.TEST_REQUEST_ID), "${%s}".format(Test.TEST_ACCOUNT_ID), "${%s}".format(Test.TEST_DOCUMENT_HASH), "${%s}".format(Test.TEST_ASSET_TYPE), 1, "${%s}".format(Test.TEST_QUANTITY_UNIT), 2).url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .pause(2)
        .exec(http("IssueAsset_POST")
          .post(routes.IssueAssetController.issueAsset().url)
          .formParamMap(Map(
            Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
            Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ACCOUNT_ID),
            Form.DOCUMENT_HASH -> "${%s}".format(Test.TEST_DOCUMENT_HASH),
            Form.ASSET_TYPE -> "${%s}".format(Test.TEST_ASSET_TYPE),
            Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
            Form.QUANTITY_UNIT -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
            Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
            Form.GAS -> "${%s}".format(Test.TEST_GAS), Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(5)
    } {
      exec { session => println("OutsideApprove" + session); session }
    }

  val masterBuyerLoginAndIssueFiatRequest: ScenarioBuilder = scenario("masterBuyerLoginAndIssueFiatRequest")
    .feed(BuyerLoginFeeder.buyerLoginFeeder)
    .exec(http("BuyerLogin_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("BuyerLogin_POST")
      .post(routes.LoginController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(2)
    .exec(controllersTest.issueFiatControllerTest.issueFiatRequestScenario)
    .pause(5)

  val masterLoginZoneAndIssueFiat: ScenarioBuilder = scenario("masterLoginZoneAndIssueFiat")
    .feed(ZoneLoginFeeder.zoneLoginFeed)
    .feed(ZoneIDFeeder.zoneIDFeed)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
    .doIfOrElse(session => getZoneStatus(session(Test.TEST_ZONE_USERNAME).as[String])) {
      exec(http("VerifiedZoneLogin_GET")
        .get(routes.LoginController.loginForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .exec(http("VerifiedZoneLogin_POST")
          .post(routes.LoginController.login().url)
          .formParamMap(Map(
            Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
            Form.NOTIFICATION_TOKEN -> "",
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(2)
        .feed(BuyerLoginFeeder.buyerLoginFeeder)
        .feed(SellerLoginFeeder.sellerLoginFeeder)
        .feed(TransactionIDFeeder.transactionIDFeed)
        .feed(TransactionAmountFeeder.transactionAmountFeed)
        .feed(PasswordFeeder.passwordFeed)
        .feed(GasFeeder.gasFeed)
        .exec { session => println("InsideIssueFiat" + ",ZONE_USERNAME" + session(Test.TEST_ZONE_USERNAME).as[String] + ",SELLER_USERNAME--" + session(Test.TEST_SELLER_USERNAME).as[String] + ",BUYER_USERNAME--" + session(Test.TEST_BUYER_USERNAME).as[String]); session }
        .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])) }
        .exec { session => session.set(Test.TEST_ACCOUNT_ID, session(Test.TEST_BUYER_USERNAME).as[String]) }
        .exec(http("IssueFiat_GET")
          .get(routes.IssueFiatController.issueFiatForm("${%s}".format(Test.TEST_REQUEST_ID), "${%s}".format(Test.TEST_ACCOUNT_ID), "${%s}".format(Test.TEST_TRANSACTION_ID), 1).url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .pause(2)
        .exec(http("IssueFiat_POST")
          .post(routes.IssueFiatController.issueFiat().url)
          .formParamMap(Map(
            Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
            Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ACCOUNT_ID),
            Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
            Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
            Form.GAS -> "${%s}".format(Test.TEST_GAS),
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(5)
    } {
      exec { session => println("OutsideApprove" + session); session }
    }

}

