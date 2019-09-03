//package controllersTest
//
//import constants.{Form, Test}
//import controllers.routes
//import controllersTest.addOrganizationControllerTest.getOrganizationID
//import controllersTest.addZoneControllerTest.{getZoneID, getZoneStatus}
//import controllersTest.changeBuyerBidControllerTest.getBuyerAddress
//import controllersTest.changeSellerBidControllerTest.getSellerAddress
//import controllersTest.issueAssetControllerTest.{getPegHashByOwnerAddress, getRequestIDForIssueAsset}
//import controllersTest.issueFiatControllerTest.getRequestIDForIssueFiat
//import controllersTest.sendCoinControllerTest.getRequestIDForFaucetRequest
//import controllersTest.setACLControllerTest._
//import feeders._
//import io.gatling.core.Predef._
//import io.gatling.core.structure.ScenarioBuilder
//import io.gatling.http.Predef._
//
//object setACLPrivileges {
//  val issueAsset = true
//  val issueFiat = true
//  val sendAsset = true
//  val sendFiat = true
//  val redeemAsset = true
//  val redeemFiat = true
//  val sellerExecuteOrder = true
//  val buyerExecuteOrder = true
//  val changeBuyerBid = true
//  val changeSellerBid = true
//  val confirmBuyerBid = true
//  val confirmSellerBid = true
//  val negotiation = true
//  val releaseAsset = true
//}
//
//object intParameters {
//  val transactionAmount = 10000
//  val assetQuantity = 2
//  val assetPrice = 1000
//}
//
//class MasterTest extends Simulation {
//
//  setUp(
//    /*For Two Users
//        masterTest.masterTestSignUp.inject(atOnceUsers(8)),
//        masterTest.masterLoginAndRequestCoin.inject(nothingFor(20), atOnceUsers(8)),
//        masterTest.masterLoginMainAndApproveFaucetRequest.inject(nothingFor(40), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1)),
//        masterTest.masterZoneLogin.inject(nothingFor(150), atOnceUsers(2)),
//        masterTest.masterLoginMainAndApproveZone.inject(nothingFor(210), atOnceUsers(1), nothingFor(15), atOnceUsers(1)),
//        masterTest.masterOrganizationLogin.inject(nothingFor(240), atOnceUsers(2)),
//        masterTest.masterLoginZoneAndApproveOrganization.inject(nothingFor(265), atOnceUsers(1), nothingFor(15), atOnceUsers(1)),
//        masterTest.masterLoginZoneAndSetACL.inject(nothingFor(295), atOnceUsers(1), nothingFor(20), atOnceUsers(1)),
//        masterTest.masterSellerLoginAndIssueAssetRequest.inject(nothingFor(340), atOnceUsers(2)),
//        masterTest.masterLoginZoneAndIssueAsset.inject(nothingFor(365), atOnceUsers(2)),
//        masterTest.masterBuyerLoginAndIssueFiatRequest.inject(nothingFor(390), atOnceUsers(2)),
//        masterTest.masterLoginZoneAndIssueFiat.inject(nothingFor(415), atOnceUsers(2)),
//    masterTest.masterTestSignUp.inject(atOnceUsers(4)),
//    masterTest.masterLoginAndRequestCoin.inject(nothingFor(15), atOnceUsers(4)),
//    masterTest.masterLoginMainAndApproveFaucetRequest.inject(nothingFor(35), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1), nothingFor(15), atOnceUsers(1)),
//    masterTest.masterZoneLogin.inject(nothingFor(90), atOnceUsers(1)),
//    masterTest.masterLoginMainAndApproveZone.inject(nothingFor(110), atOnceUsers(1)),
//    masterTest.masterOrganizationLogin.inject(nothingFor(130), atOnceUsers(1)),
//    masterTest.masterLoginZoneAndApproveOrganization.inject(nothingFor(160), atOnceUsers(1)),
//    masterTest.masterLoginZoneAndSetACL.inject(nothingFor(180), atOnceUsers(1)),
//    masterTest.masterSellerLoginAndIssueAssetRequest.inject(nothingFor(220), atOnceUsers(1)),
//    masterTest.masterLoginZoneAndIssueAsset.inject(nothingFor(245), atOnceUsers(1)),
//    masterTest.masterBuyerLoginAndIssueFiatRequest.inject(nothingFor(265), atOnceUsers(1)),
//    masterTest.masterLoginZoneAndIssueFiat.inject(nothingFor(275), atOnceUsers(1)),
//    masterTest.masterBuyerLoginAndChangeBuyerBid.inject(nothingFor(300), atOnceUsers(1)),
//    masterTest.masterSellerLoginAndChangeSellerBid.inject(nothingFor(320), atOnceUsers(1)),
//    masterTest.masterBuyerLoginAndConfirmBuyerBid.inject(nothingFor(340), atOnceUsers(1)),
//    masterTest.masterSellerLoginAndConfirmSellerBid.inject(nothingFor(360), atOnceUsers(1)),
//    masterTest.masterLoginZoneAndReleaseAsset.inject(nothingFor(390), atOnceUsers(1)),
//    masterTest.masterBuyerLoginAndSendFiat.inject(nothingFor(420), atOnceUsers(1)),
//    masterTest.masterSellerLoginAndSendAsset.inject(nothingFor(440), atOnceUsers(1)),
//    masterTest.masterLoginZoneAndExecuteOrder.inject(nothingFor(460), atOnceUsers(1))
//  ).protocols(http.baseUrl(Test.BASE_URL))
//}
//
//object masterTest {
//
//  val masterTestSignUp: ScenarioBuilder = scenario("masterTestSignUp")
//    .feed(UsernameFeeder.usernameFeed)
//    .feed(PasswordFeeder.passwordFeed)
//    .exec(http("SignUp_GET")
//      .get(routes.SignUpController.signUpForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .exec(http("SignUp_POST")
//      .post(routes.SignUpController.signUp().url)
//      .formParamMap(Map(
//        Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//
//  val masterLoginAndRequestCoin: ScenarioBuilder = scenario("masterLoginAndRequestCoin")
//    .feed(UsernameFeeder.usernameFeed)
//    .feed(PasswordFeeder.passwordFeed)
//    .exec(http("Login_GET")
//      .get(routes.LoginController.loginForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .exec(http("Login_POST")
//      .post(routes.LoginController.login().url)
//      .formParamMap(Map(
//        Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
//        Form.NOTIFICATION_TOKEN -> "",
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//    .feed(CouponFeeder.couponFeed)
//    .exec(http("RequestCoin_GET")
//      .get(routes.SendCoinController.requestCoinsForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("RequestCoin_POST")
//      .post(routes.SendCoinController.requestCoins().url)
//      .formParamMap(Map(
//        Form.COUPON -> "${%s}".format(Test.TEST_COUPON),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//
//  val masterLoginMainAndApproveFaucetRequest: ScenarioBuilder = scenario("masterLoginMainAndApproveFaucetRequest")
//    .feed(GenesisFeeder.genesisFeed)
//    .feed(AccountIDFeeder.accountIDFeed)
//    .feed(RequestIDFeeder.requestIDFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec(http("MainLogin_GET")
//      .get(routes.LoginController.loginForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .exec(http("MainLogin_POST")
//      .post(routes.LoginController.login().url)
//      .formParamMap(Map(
//        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
//        Form.NOTIFICATION_TOKEN -> "",
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(2)
//    .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_ACCOUNT_ID).as[String])) }
//    .exec(http("ApproveFaucetRequest_GET")
//      .get(routes.SendCoinController.approveFaucetRequestsForm("${%s}".format(Test.TEST_REQUEST_ID), "${%s}".format(Test.TEST_ACCOUNT_ID)).url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("ApproveFaucetRequest_POST")
//      .post(routes.SendCoinController.approveFaucetRequests().url)
//      .formParamMap(Map(
//        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
//        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ACCOUNT_ID),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//
//  val masterZoneLogin: ScenarioBuilder = scenario("masterZoneLogin")
//    .feed(ZoneLoginFeeder.zoneLoginFeed)
//    .exec(http("ZoneLogin_GET")
//      .get(routes.LoginController.loginForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .exec(http("ZoneLogin_POST")
//      .post(routes.LoginController.login().url)
//      .formParamMap(Map(
//        Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//        Form.NOTIFICATION_TOKEN -> "",
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//    .feed(NameFeeder.nameFeed)
//    .feed(CurrencyFeeder.currencyFeed)
//    .exec(http("AddZone_GET")
//      .get(routes.AddZoneController.addZoneForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("AddZone_POST")
//      .post(routes.AddZoneController.addZone().url)
//      .formParamMap(Map(
//        Form.NAME -> "${%s}".format(Test.TEST_NAME),
//        Form.CURRENCY -> "${%s}".format(Test.TEST_CURRENCY),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//
//  val masterLoginMainAndApproveZone: ScenarioBuilder = scenario("masterLoginMainAndApproveZone")
//    .feed(GenesisFeeder.genesisFeed)
//    .feed(ZoneLoginFeeder.zoneLoginFeed)
//    .feed(ZoneIDFeeder.zoneIDFeed)
//    .exec(http("MainLogin_GET")
//      .get(routes.LoginController.loginForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .exec(http("MainLogin_POST")
//      .post(routes.LoginController.login().url)
//      .formParamMap(Map(
//        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
//        Form.NOTIFICATION_TOKEN -> "",
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(2)
//    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
//    .exec(http("VerifyZone_GET")
//      .get(routes.AddZoneController.verifyZoneForm("${%s}".format(Test.TEST_ZONE_ID)).url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("VerifyZone_POST")
//      .post(routes.AddZoneController.verifyZone().url)
//      .formParamMap(Map(
//        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//
//  val masterOrganizationLogin: ScenarioBuilder = scenario("masterOrganizationLogin")
//    .feed(OrganizationLoginFeeder.organizationLoginFeed)
//    .feed(ZoneLoginFeeder.zoneLoginFeed)
//    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
//    .doIfOrElse(session => getZoneStatus(session(Test.TEST_ZONE_USERNAME).as[String])) {
//      exec(http("OrganizationLogin_GET")
//        .get(routes.LoginController.loginForm().url)
//        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .exec(http("OrganizationLogin_POST")
//          .post(routes.LoginController.login().url)
//          .formParamMap(Map(
//            Form.USERNAME -> "${%s}".format(Test.TEST_ORGANIZATION_USERNAME),
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ORGANIZATION_PASSWORD),
//            Form.NOTIFICATION_TOKEN -> "",
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(5)
//        .feed(NameFeeder.nameFeed)
//        .feed(AddressFeeder.addressFeed)
//        .feed(EmailAddressFeeder.emailAddressFeed)
//        .feed(MobileNumberFeeder.mobileNumberFeed)
//        .exec(http("AddOrganization_GET")
//          .get(routes.AddOrganizationController.addOrganizationForm().url)
//          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .pause(2)
//        .exec(http("AddOrganization_POST")
//          .post(routes.AddOrganizationController.addOrganizationForm().url)
//          .formParamMap(Map(
//            Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
//            Form.NAME -> "${%s}".format(Test.TEST_NAME),
//            Form.ADDRESS -> "${%s}".format(Test.TEST_ADDRESS),
//            Form.EMAIL -> "${%s}".format(Test.TEST_EMAIL_ADDRESS),
//            Form.PHONE -> "${%s}".format(Test.TEST_MOBILE_NUMBER),
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(5)
//    } {
//      exec { session => println("OutsideLogin" + session); session }
//    }
//
//  val masterLoginZoneAndApproveOrganization: ScenarioBuilder = scenario("masterLoginZoneAndApproveOrganization")
//    .feed(ZoneLoginFeeder.zoneLoginFeed)
//    .feed(OrganizationLoginFeeder.organizationLoginFeed)
//    .feed(ZoneIDFeeder.zoneIDFeed)
//    .feed(OrganizationIDFeeder.organizationIDFeed)
//    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
//    .doIfOrElse(session => getZoneStatus(session(Test.TEST_ZONE_USERNAME).as[String])) {
//      exec(http("VerifiedZoneLogin_GET")
//        .get(routes.LoginController.loginForm().url)
//        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .exec(http("VerifiedZoneLogin_POST")
//          .post(routes.LoginController.login().url)
//          .formParamMap(Map(
//            Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.NOTIFICATION_TOKEN -> "",
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(2)
//        .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
//        .exec(http("VerifyOrganization_GET")
//          .get(routes.AddOrganizationController.verifyOrganizationForm("${%s}".format(Test.TEST_ORGANIZATION_ID), "${%s}".format(Test.TEST_ZONE_ID)).url)
//          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .pause(2)
//        .exec(http("VerifyOrganization_POST")
//          .post(routes.AddOrganizationController.verifyOrganization().url)
//          .formParamMap(Map(
//            Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
//            Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(5)
//    } {
//      exec { session => println("OutsideApprove" + session); session }
//    }
//
//  val masterLoginZoneAndSetACL: ScenarioBuilder = scenario("masterLoginZoneAndSetACL")
//    .feed(ZoneLoginFeeder.zoneLoginFeed)
//    .feed(OrganizationLoginFeeder.organizationLoginFeed)
//    .feed(ZoneIDFeeder.zoneIDFeed)
//    .feed(OrganizationIDFeeder.organizationIDFeed)
//    .feed(SellerLoginFeeder.sellerLoginFeed)
//    .feed(BuyerLoginFeeder.buyerLoginFeed)
//    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
//    .doIfOrElse(session => getZoneStatus(session(Test.TEST_ZONE_USERNAME).as[String])) {
//      exec(http("VerifiedZoneLogin_GET")
//        .get(routes.LoginController.loginForm().url)
//        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .exec(http("VerifiedZoneLogin_POST")
//          .post(routes.LoginController.login().url)
//          .formParamMap(Map(
//            Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.NOTIFICATION_TOKEN -> "",
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(2)
//        .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
//        .exec(http("SetACLSeller_GET")
//          .get(routes.SetACLController.setACLForm().url)
//          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .pause(2)
//        .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getAccountAddressByUsername(session(Test.TEST_SELLER_USERNAME).as[String])) }
//        .exec(http("SetACLSeller_POST")
//          .post(routes.SetACLController.setACL().url)
//          .formParamMap(Map(
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.ACL_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
//            Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
//            Form.ISSUE_ASSET -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT -> setACLPrivileges.issueFiat, Form.SEND_ASSET -> setACLPrivileges.sendAsset, Form.SEND_FIAT -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION -> setACLPrivileges.negotiation, Form.RELEASE_ASSET -> setACLPrivileges.releaseAsset,
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(10)
//        .exec(http("SetACLBuyer_GET")
//          .get(routes.SetACLController.setACLForm().url)
//          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .pause(2)
//        .exec { session => session.set(Test.TEST_BUYER_ADDRESS, getAccountAddressByUsername(session(Test.TEST_BUYER_USERNAME).as[String])) }
//        .exec(http("SetACLBuyer_POST")
//          .post(routes.SetACLController.setACL().url)
//          .formParamMap(Map(
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.ACL_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
//            Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
//            Form.ISSUE_ASSET -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT -> setACLPrivileges.issueFiat, Form.SEND_ASSET -> setACLPrivileges.sendAsset, Form.SEND_FIAT -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION -> setACLPrivileges.negotiation, Form.RELEASE_ASSET -> setACLPrivileges.releaseAsset,
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(5)
//    } {
//      exec { session => println("OutsideApprove" + session); session }
//    }
//
//  val masterSellerLoginAndIssueAssetRequest: ScenarioBuilder = scenario("masterSellerLoginAndIssueAssetRequest")
//    .feed(SellerLoginFeeder.sellerLoginFeed)
//    .exec(http("SellerLogin_GET")
//      .get(routes.LoginController.loginForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .exec(http("SellerLogin_POST")
//      .post(routes.LoginController.login().url)
//      .formParamMap(Map(
//        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
//        Form.NOTIFICATION_TOKEN -> "",
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(2)
//    .feed(DocumentHashFeeder.documentHashFeed)
//    .feed(AssetTypeFeeder.assetTypeFeed)
//    .feed(AssetPriceFeeder.assetPriceFeed)
//    .feed(QuantityUnitFeeder.quantityUnitFeed)
//    .feed(AssetQuantityFeeder.assetQuantityFeed)
//    .exec(http("IssueAssetRequest_GET")
//      .get(routes.IssueAssetController.issueAssetRequestForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("IssueAssetRequest_POST")
//      .post(routes.IssueAssetController.issueAssetRequest().url)
//      .formParamMap(Map(
//        Form.DOCUMENT_HASH -> "${%s}".format(Test.TEST_DOCUMENT_HASH),
//        Form.ASSET_TYPE -> "${%s}".format(Test.TEST_ASSET_TYPE),
//        Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
//        Form.QUANTITY_UNIT -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
//        Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//
//  val masterLoginZoneAndIssueAsset: ScenarioBuilder = scenario("masterLoginZoneAndIssueAsset")
//    .feed(ZoneLoginFeeder.zoneLoginFeed.queue)
//    .feed(ZoneIDFeeder.zoneIDFeed)
//    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
//    .doIfOrElse(session => getZoneStatus(session(Test.TEST_ZONE_USERNAME).as[String])) {
//      exec(http("VerifiedZoneLogin_GET")
//        .get(routes.LoginController.loginForm().url)
//        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .exec(http("VerifiedZoneLogin_POST")
//          .post(routes.LoginController.login().url)
//          .formParamMap(Map(
//            Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.NOTIFICATION_TOKEN -> "",
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(2)
//        .feed(SellerLoginFeeder.sellerLoginFeed)
//        .feed(BuyerLoginFeeder.buyerLoginFeed)
//        .feed(DocumentHashFeeder.documentHashFeed)
//        .feed(AssetTypeFeeder.assetTypeFeed)
//        .feed(AssetPriceFeeder.assetPriceFeed)
//        .feed(QuantityUnitFeeder.quantityUnitFeed)
//        .feed(AssetQuantityFeeder.assetQuantityFeed)
//        .feed(PasswordFeeder.passwordFeed)
//        .feed(GasFeeder.gasFeed)
//        .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])) }
//        .exec { session => session.set(Test.TEST_ACCOUNT_ID, session(Test.TEST_SELLER_USERNAME).as[String]) }
//        .exec(http("IssueAsset_GET")
//          .get(routes.IssueAssetController.issueAssetForm("${%s}".format(Test.TEST_REQUEST_ID), "${%s}".format(Test.TEST_ACCOUNT_ID), "${%s}".format(Test.TEST_DOCUMENT_HASH), "${%s}".format(Test.TEST_ASSET_TYPE), intParameters.assetPrice, "${%s}".format(Test.TEST_QUANTITY_UNIT), intParameters.assetQuantity).url)
//          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .pause(2)
//        .exec(http("IssueAsset_POST")
//          .post(routes.IssueAssetController.issueAsset().url)
//          .formParamMap(Map(
//            Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
//            Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ACCOUNT_ID),
//            Form.DOCUMENT_HASH -> "${%s}".format(Test.TEST_DOCUMENT_HASH),
//            Form.ASSET_TYPE -> "${%s}".format(Test.TEST_ASSET_TYPE),
//            Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
//            Form.QUANTITY_UNIT -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
//            Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.GAS -> "${%s}".format(Test.TEST_GAS), Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(5)
//    } {
//      exec { session => println("OutsideApprove" + session); session }
//    }
//
//  val masterBuyerLoginAndIssueFiatRequest: ScenarioBuilder = scenario("masterBuyerLoginAndIssueFiatRequest")
//    .feed(BuyerLoginFeeder.buyerLoginFeed)
//    .exec(http("BuyerLogin_GET")
//      .get(routes.LoginController.loginForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .exec(http("BuyerLogin_POST")
//      .post(routes.LoginController.login().url)
//      .formParamMap(Map(
//        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
//        Form.NOTIFICATION_TOKEN -> "",
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(2)
//    .feed(TransactionIDFeeder.transactionIDFeed)
//    .feed(TransactionAmountFeeder.transactionAmountFeed)
//    .exec(http("IssueFiatRequest_GET")
//      .get(routes.IssueFiatController.issueFiatRequestForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("IssueFiatRequest_POST")
//      .post(routes.IssueFiatController.issueFiatRequest().url)
//      .formParamMap(Map(
//        Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
//        Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//
//  val masterLoginZoneAndIssueFiat: ScenarioBuilder = scenario("masterLoginZoneAndIssueFiat")
//    .feed(ZoneLoginFeeder.zoneLoginFeed)
//    .feed(ZoneIDFeeder.zoneIDFeed)
//    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
//    .doIfOrElse(session => getZoneStatus(session(Test.TEST_ZONE_USERNAME).as[String])) {
//      exec(http("VerifiedZoneLogin_GET")
//        .get(routes.LoginController.loginForm().url)
//        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .exec(http("VerifiedZoneLogin_POST")
//          .post(routes.LoginController.login().url)
//          .formParamMap(Map(
//            Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.NOTIFICATION_TOKEN -> "",
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(2)
//        .feed(BuyerLoginFeeder.buyerLoginFeed)
//        .feed(SellerLoginFeeder.sellerLoginFeed)
//        .feed(TransactionIDFeeder.transactionIDFeed)
//        .feed(RequestIDFeeder.requestIDFeed)
//        .feed(TransactionAmountFeeder.transactionAmountFeed)
//        .feed(PasswordFeeder.passwordFeed)
//        .feed(GasFeeder.gasFeed)
//        .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])) }
//        .exec { session => session.set(Test.TEST_ACCOUNT_ID, session(Test.TEST_BUYER_USERNAME).as[String]) }
//        .exec(http("IssueFiat_GET")
//          .get(routes.IssueFiatController.issueFiatForm("${%s}".format(Test.TEST_REQUEST_ID), "${%s}".format(Test.TEST_ACCOUNT_ID), "${%s}".format(Test.TEST_TRANSACTION_ID), intParameters.transactionAmount).url)
//          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .pause(2)
//        .exec(http("IssueFiat_POST")
//          .post(routes.IssueFiatController.issueFiat().url)
//          .formParamMap(Map(
//            Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
//            Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ACCOUNT_ID),
//            Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
//            Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.GAS -> "${%s}".format(Test.TEST_GAS),
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(5)
//    } {
//      exec { session => println("OutsideApprove" + session); session }
//    }
//
//  val masterBuyerLoginAndChangeBuyerBid: ScenarioBuilder = scenario("masterBuyerLoginAndChangeBuyerBid")
//    .feed(BuyerLoginFeeder.buyerLoginFeed)
//    .exec(http("BuyerLogin_GET")
//      .get(routes.LoginController.loginForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .exec(http("BuyerLogin_POST")
//      .post(routes.LoginController.login().url)
//      .formParamMap(Map(
//        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
//        Form.NOTIFICATION_TOKEN -> "",
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(2)
//    .feed(SellerLoginFeeder.sellerLoginFeed)
//    .feed(SellerAddressFeeder.sellerAddressFeed)
//    .feed(PasswordFeeder.passwordFeed)
//    .feed(BuyerBidFeeder.buyerBidFeed)
//    .feed(TimeFeeder.timeFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
//    .exec { session => session.set(Test.TEST_BID, session(Test.TEST_BUYER_BID).as[String]) }
//    .exec { session => session.set(Test.TEST_TIME, session(Test.TEST_TIME).as[String]) }
//    .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
//    .exec(http("ChangeBuyerBid_GET")
//      .get(routes.ChangeBuyerBidController.changeBuyerBidForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("ChangeBuyerBid_POST")
//      .post(routes.ChangeBuyerBidController.changeBuyerBid().url)
//      .formParamMap(Map(
//        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
//        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
//        Form.BID -> "${%s}".format(Test.TEST_BID),
//        Form.TIME -> "${%s}".format(Test.TEST_TIME),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//
//  val masterSellerLoginAndChangeSellerBid: ScenarioBuilder = scenario("masterSellerLoginAndChangeSellerBid")
//    .feed(SellerLoginFeeder.sellerLoginFeed)
//    .exec(http("SellerLogin_GET")
//      .get(routes.LoginController.loginForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .exec(http("SellerLogin_POST")
//      .post(routes.LoginController.login().url)
//      .formParamMap(Map(
//        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
//        Form.NOTIFICATION_TOKEN -> "",
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(2)
//    .feed(BuyerLoginFeeder.buyerLoginFeed)
//    .feed(BuyerAddressFeeder.buyerAddressFeed)
//    .feed(SellerAddressFeeder.sellerAddressFeed)
//    .feed(SellerBidFeeder.sellerBidFeed)
//    .feed(TimeFeeder.timeFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(GasFeeder.gasFeed)
//    .feed(FromFeeder.fromFeed)
//    .exec { session => session.set(Test.TEST_BUYER_ADDRESS, getBuyerAddress(session(Test.TEST_BUYER_USERNAME).as[String])) }
//    .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
//    .exec { session => session.set(Test.TEST_BID, session(Test.TEST_SELLER_BID).as[String]) }
//    .exec { session => session.set(Test.TEST_TIME, session(Test.TEST_TIME).as[String]) }
//    .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
//    .exec(http("ChangeSellerBid_GET")
//      .get(routes.ChangeSellerBidController.changeSellerBidForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("ChangeSellerBid_POST")
//      .post(routes.ChangeSellerBidController.changeSellerBid().url)
//      .formParamMap(Map(
//        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
//        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
//        Form.BID -> "${%s}".format(Test.TEST_BID),
//        Form.TIME -> "${%s}".format(Test.TEST_TIME),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//
//  val masterBuyerLoginAndConfirmBuyerBid: ScenarioBuilder = scenario("masterBuyerLoginAndConfirmBuyerBid")
//    .feed(BuyerLoginFeeder.buyerLoginFeed)
//    .exec(http("BuyerLogin_GET")
//      .get(routes.LoginController.loginForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .exec(http("BuyerLogin_POST")
//      .post(routes.LoginController.login().url)
//      .formParamMap(Map(
//        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
//        Form.NOTIFICATION_TOKEN -> "",
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(2)
//    .feed(SellerLoginFeeder.sellerLoginFeed)
//    .feed(SellerAddressFeeder.sellerAddressFeed)
//    .feed(PasswordFeeder.passwordFeed)
//    .feed(SellerBidFeeder.sellerBidFeed)
//    .feed(TimeFeeder.timeFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
//    .exec { session => session.set(Test.TEST_BID, session(Test.TEST_SELLER_BID).as[String]) }
//    .exec { session => session.set(Test.TEST_TIME, session(Test.TEST_TIME).as[String]) }
//    .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
//    .exec(http("ConfirmBuyerBid_GET")
//      .get(routes.ConfirmBuyerBidController.confirmBuyerBidForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("ConfirmBuyerBid_POST")
//      .post(routes.ConfirmBuyerBidController.confirmBuyerBid().url)
//      .formParamMap(Map(
//        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
//        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
//        Form.BID -> "${%s}".format(Test.TEST_BID),
//        Form.TIME -> "${%s}".format(Test.TEST_TIME),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//
//  val masterSellerLoginAndConfirmSellerBid: ScenarioBuilder = scenario("masterSellerLoginAndConfirmSellerBid")
//    .feed(SellerLoginFeeder.sellerLoginFeed)
//    .exec(http("SellerLogin_GET")
//      .get(routes.LoginController.loginForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .exec(http("SellerLogin_POST")
//      .post(routes.LoginController.login().url)
//      .formParamMap(Map(
//        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
//        Form.NOTIFICATION_TOKEN -> "",
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(2)
//    .feed(BuyerLoginFeeder.buyerLoginFeed)
//    .feed(BuyerAddressFeeder.buyerAddressFeed)
//    .feed(SellerAddressFeeder.sellerAddressFeed)
//    .feed(SellerBidFeeder.sellerBidFeed)
//    .feed(TimeFeeder.timeFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(GasFeeder.gasFeed)
//    .feed(FromFeeder.fromFeed)
//    .exec { session => session.set(Test.TEST_BUYER_ADDRESS, getBuyerAddress(session(Test.TEST_BUYER_USERNAME).as[String])) }
//    .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
//    .exec { session => session.set(Test.TEST_BID, session(Test.TEST_SELLER_BID).as[String]) }
//    .exec { session => session.set(Test.TEST_TIME, session(Test.TEST_TIME).as[String]) }
//    .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
//    .exec(http("ConfirmSellerBid_GET")
//      .get(routes.ConfirmSellerBidController.confirmSellerBidForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("ConfirmSellerBid_POST")
//      .post(routes.ConfirmSellerBidController.confirmSellerBid().url)
//      .formParamMap(Map(
//        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
//        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
//        Form.BID -> "${%s}".format(Test.TEST_BID),
//        Form.TIME -> "${%s}".format(Test.TEST_TIME),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//
//  val masterLoginZoneAndReleaseAsset: ScenarioBuilder = scenario("masterLoginZoneAndReleaseAsset")
//    .feed(ZoneLoginFeeder.zoneLoginFeed)
//    .doIfOrElse(session => getZoneStatus(session(Test.TEST_ZONE_USERNAME).as[String])) {
//      exec(http("VerifiedZoneLogin_GET")
//        .get(routes.LoginController.loginForm().url)
//        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .exec(http("VerifiedZoneLogin_POST")
//          .post(routes.LoginController.login().url)
//          .formParamMap(Map(
//            Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.NOTIFICATION_TOKEN -> "",
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(2)
//        .feed(SellerLoginFeeder.sellerLoginFeed)
//        .feed(SellerAddressFeeder.sellerAddressFeed)
//        .feed(PegHashFeeder.pegHashFeed)
//        .feed(PasswordFeeder.passwordFeed)
//        .feed(GasFeeder.gasFeed)
//        .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
//        .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
//        .exec(http("ReleaseAsset_GET")
//          .post(routes.ReleaseAssetController.releaseAssetForm().url)
//          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .pause(2)
//        .exec(http("ReleaseAsset_POST")
//          .post(routes.ReleaseAssetController.releaseAsset().url)
//          .formParamMap(Map(
//            Form.ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
//            Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.GAS -> "${%s}".format(Test.TEST_GAS),
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(5)
//    } {
//      exec { session => println("OutsideApprove" + session); session }
//    }
//
//  val masterSellerLoginAndSendAsset: ScenarioBuilder = scenario("masterSellerLoginAndSendAsset")
//    .feed(SellerLoginFeeder.sellerLoginFeed)
//    .exec(http("SellerLogin_GET")
//      .get(routes.LoginController.loginForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .exec(http("SellerLogin_POST")
//      .post(routes.LoginController.login().url)
//      .formParamMap(Map(
//        Form.USERNAME -> "${%s}".format(Test.TEST_SELLER_USERNAME),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
//        Form.NOTIFICATION_TOKEN -> "",
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(2)
//    .feed(SellerAddressFeeder.sellerAddressFeed)
//    .feed(BuyerLoginFeeder.buyerLoginFeed)
//    .feed(BuyerAddressFeeder.buyerAddressFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec { session => session.set(Test.TEST_BUYER_ADDRESS, getBuyerAddress(session(Test.TEST_BUYER_USERNAME).as[String])) }
//    .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
//    .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
//    .exec(http("SendAsset_GET")
//      .get(routes.SendAssetController.sendAssetForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("SendAsset_POST")
//      .post(routes.SendAssetController.sendAsset().url)
//      .formParamMap(Map(
//        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
//        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//
//  val masterBuyerLoginAndSendFiat: ScenarioBuilder = scenario("masterBuyerLoginAndSendFiat")
//    .feed(BuyerLoginFeeder.buyerLoginFeed)
//    .exec(http("BuyerLogin_GET")
//      .get(routes.LoginController.loginForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .exec(http("BuyerLogin_POST")
//      .post(routes.LoginController.login().url)
//      .formParamMap(Map(
//        Form.USERNAME -> "${%s}".format(Test.TEST_BUYER_USERNAME),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
//        Form.NOTIFICATION_TOKEN -> "",
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(2)
//    .feed(SellerLoginFeeder.sellerLoginFeed)
//    .feed(SellerAddressFeeder.sellerAddressFeed)
//    .feed(AmountFeeder.amountFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(SellerBidFeeder.sellerBidFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
//    .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
//    .exec { session => session.set(Test.TEST_BID, session(Test.TEST_SELLER_BID).as[String]) }
//    .exec(http("SendFiat_GET")
//      .get(routes.SendFiatController.sendFiatForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("SendFiat_POST")
//      .get(routes.SendFiatController.sendFiat().url)
//      .formParamMap(Map(
//        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
//        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
//        Form.AMOUNT -> "${%s}".format(Test.TEST_AMOUNT),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//    .pause(5)
//
//  val masterLoginZoneAndExecuteOrder: ScenarioBuilder = scenario("masterLoginZoneAndExecuteOrder")
//    .feed(ZoneLoginFeeder.zoneLoginFeed)
//    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(session(Test.TEST_ZONE_USERNAME).as[String])) }
//    .doIfOrElse(session => getZoneStatus(session(Test.TEST_ZONE_USERNAME).as[String])) {
//      exec(http("VerifiedZoneLogin_GET")
//        .get(routes.LoginController.loginForm().url)
//        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .exec(http("VerifiedZoneLogin_POST")
//          .post(routes.LoginController.login().url)
//          .formParamMap(Map(
//            Form.USERNAME -> "${%s}".format(Test.TEST_ZONE_USERNAME),
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.NOTIFICATION_TOKEN -> "",
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(2)
//        .feed(BuyerLoginFeeder.buyerLoginFeed)
//        .feed(BuyerAddressFeeder.buyerAddressFeed)
//        .feed(SellerLoginFeeder.sellerLoginFeed)
//        .feed(SellerAddressFeeder.sellerAddressFeed)
//        .feed(FiatProofHashFeeder.fiatProofHashFeed)
//        .feed(PegHashFeeder.pegHashFeed)
//        .feed(GasFeeder.gasFeed)
//        .feed(AWBProofHashFeeder.awbProofHashFeed)
//        .exec { session => session.set(Test.TEST_BUYER_ADDRESS, getBuyerAddress(session(Test.TEST_BUYER_USERNAME).as[String])) }
//        .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
//        .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
//        .exec(http("BuyerExecuteOrder_GET")
//          .get(routes.BuyerExecuteOrderController.buyerExecuteOrderForm().url)
//          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .pause(2)
//        .exec(http("BuyerExecuteOrder_POST")
//          .get(routes.BuyerExecuteOrderController.buyerExecuteOrder().url)
//          .formParamMap(Map(
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
//            Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
//            Form.FIAT_PROOF_HASH -> "${%s}".format(Test.TEST_FIAT_PROOF_HASH),
//            Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//            Form.GAS -> "${%s}".format(Test.TEST_GAS),
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(10)
//        .feed(SellerLoginFeeder.sellerLoginFeed)
//        .exec(http("SellerExecuteOrder_GET")
//          .get(routes.SellerExecuteOrderController.sellerExecuteOrderForm().url)
//          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//        .pause(2)
//        .exec(http("SellerExecuteOrder_POST")
//          .get(routes.SellerExecuteOrderController.sellerExecuteOrder().url)
//          .formParamMap(Map(
//            Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD),
//            Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
//            Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
//            Form.AWB_PROOF_HASH -> "${%s}".format(Test.TEST_AWB_PROOF_HASH),
//            Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//            Form.GAS -> "${%s}".format(Test.TEST_GAS),
//            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//        .pause(5)
//    } {
//      exec { session => println("OutsideApprove" + session); session }
//    }
//}
//
//*/
