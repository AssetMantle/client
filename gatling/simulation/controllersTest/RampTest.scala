package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.addOrganizationControllerTest.getOrganizationID
import controllersTest.addZoneControllerTest.{getZoneID, getZoneStatus}
import controllersTest.changeBuyerBidControllerTest.getBuyerAddress
import controllersTest.changeSellerBidControllerTest.getSellerAddress
import controllersTest.issueAssetControllerTest.{getPegHashByOwnerAddress, getRequestIDForIssueAsset}
import controllersTest.issueFiatControllerTest.getRequestIDForIssueFiat
import controllersTest.sendCoinControllerTest.getRequestIDForFaucetRequest
import controllersTest.setACLControllerTest.getAccountAddressByUsername
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class RampTest extends Simulation {

  val scenarioBuilderZoneOrganization: ScenarioBuilder =
    scenario("Zone/Organization Flow")
    .exec(zoneSignUp.masterZoneSignUp)
    .exec(organizationSignUp.masterOrganizationSignUp)
    .exec(zoneLoginRequest.masterZoneLoginRequest)
    .exec(loginMainAndApproveZone.masterLoginMainAndApproveZone)
    .exec(OrganizationLoginRequest.masterOrganizationLoginRequest)
    .exec(loginZoneAndApproveOrganization.masterLoginZoneAndApproveOrganization)
    .exec(loginZoneAndSetACL.masterLoginZoneAndSetACL)

  val scenarioBuilderTraders:ScenarioBuilder =
    scenario("Trader Flow")
    .exec(traderSignUp.masterTraderSignUp)
    .exec(traderLoginAndRequestCoin.masterTraderLoginAndRequestCoin)

  setUp(
    scenarioBuilderZoneOrganization.inject(rampUsers(10) during 15),
    scenarioBuilderTraders.inject(nothingFor(100), rampUsers(10) during 15)
  ).protocols(http.baseUrl(Test.BASE_URL))
}

object zoneSignUp {

  val masterZoneSignUp: ScenarioBuilder = scenario("masterZoneSignUp")
    .exec(http("ZoneSignUp_GET")
      .get(routes.SignUpController.signUpForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(5)
    .exec(http("ZoneSignUp_POST")
      .post(routes.SignUpController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> Test.TEST_ZONE_USERNAME_UNIQUE,
        Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

}

object organizationSignUp {

  val masterOrganizationSignUp: ScenarioBuilder = scenario("masterOrganizationSignUp")
    .exec(http("OrganizationSignUp_GET")
      .get(routes.SignUpController.signUpForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(5)
    .exec(http("OrganizationSignUp_POST")
      .post(routes.SignUpController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> Test.TEST_ORGANIZATION_USERNAME_UNIQUE,
        Form.PASSWORD -> Test.TEST_ORGANIZATION_PASSWORD_UNIQUE,
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

}

object zoneLoginRequest{

  val masterZoneLoginRequest: ScenarioBuilder = scenario("masterZoneLoginRequest")
    .exec(http("ZoneLogin_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("ZoneLogin_POST")
      .post(routes.LoginController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> Test.TEST_ZONE_USERNAME_UNIQUE,
        Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
        Form.NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
    .feed(NameFeeder.nameFeed)
    .feed(CurrencyFeeder.currencyFeed)
    .exec(http("AddZone_GET")
      .get(routes.AddZoneController.addZoneForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("AddZone_POST")
      .post(routes.AddZoneController.addZone().url)
      .formParamMap(Map(
        Form.NAME -> "${%s}".format(Test.TEST_NAME),
        Form.CURRENCY -> "${%s}".format(Test.TEST_CURRENCY),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}

object loginMainAndApproveZone{

  val masterLoginMainAndApproveZone: ScenarioBuilder = scenario("masterLoginMainAndApproveZone")
    .feed(GenesisFeeder.genesisFeed)
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
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(Test.TEST_ZONE_USERNAME_UNIQUE)) }
    .exec(http("VerifyZone_GET")
      .get(routes.AddZoneController.verifyZoneForm("${%s}".format(Test.TEST_ZONE_ID)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("VerifyZone_POST")
      .post(routes.AddZoneController.verifyZone().url)
      .formParamMap(Map(
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}

object OrganizationLoginRequest{
val masterOrganizationLoginRequest: ScenarioBuilder = scenario("masterOrganizationLoginRequest")
  .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(Test.TEST_ZONE_USERNAME_UNIQUE)) }
  .doIfOrElse(session => getZoneStatus(Test.TEST_ZONE_USERNAME_UNIQUE)) {
    exec(http("OrganizationLogin_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
      .exec(http("OrganizationLogin_POST")
        .post(routes.LoginController.login().url)
        .formParamMap(Map(
          Form.USERNAME -> Test.TEST_ORGANIZATION_USERNAME_UNIQUE,
          Form.PASSWORD -> Test.TEST_ORGANIZATION_PASSWORD_UNIQUE,
          Form.NOTIFICATION_TOKEN -> "",
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
      .pause(5)
      .feed(NameFeeder.nameFeed)
      .feed(AddressFeeder.addressFeed)
      .feed(EmailAddressFeeder.emailAddressFeed)
      .feed(MobileNumberFeeder.mobileNumberFeed)
      .exec(http("AddOrganization_GET")
        .get(routes.AddOrganizationController.addOrganizationForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
      .pause(2)
      .exec(http("AddOrganization_POST")
        .post(routes.AddOrganizationController.addOrganizationForm().url)
        .formParamMap(Map(
          Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
          Form.NAME -> "${%s}".format(Test.TEST_NAME),
          Form.ADDRESS -> "${%s}".format(Test.TEST_ADDRESS),
          Form.EMAIL -> "${%s}".format(Test.TEST_EMAIL_ADDRESS),
          Form.PHONE -> "${%s}".format(Test.TEST_MOBILE_NUMBER),
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
      .pause(5)
  } {
    exec { session => println("OutsideLogin" + session); session }
  }
}

object loginZoneAndApproveOrganization{

  val masterLoginZoneAndApproveOrganization: ScenarioBuilder = scenario("masterLoginZoneAndApproveOrganization")
    .feed(ZoneIDFeeder.zoneIDFeed)
    .feed(OrganizationIDFeeder.organizationIDFeed)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(Test.TEST_ZONE_USERNAME_UNIQUE)) }
    .doIfOrElse(session => getZoneStatus(Test.TEST_ZONE_USERNAME_UNIQUE)) {
      exec(http("VerifiedZoneLogin_GET")
        .get(routes.LoginController.loginForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .exec(http("VerifiedZoneLogin_POST")
          .post(routes.LoginController.login().url)
          .formParamMap(Map(
            Form.USERNAME -> Test.TEST_ZONE_USERNAME_UNIQUE,
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
            Form.NOTIFICATION_TOKEN -> "",
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(2)
        .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_ORGANIZATION_USERNAME).as[String])) }
        .exec(http("VerifyOrganization_GET")
          .get(routes.AddOrganizationController.verifyOrganizationForm("${%s}".format(Test.TEST_ORGANIZATION_ID), "${%s}".format(Test.TEST_ZONE_ID)).url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .pause(2)
        .exec(http("VerifyOrganization_POST")
          .post(routes.AddOrganizationController.verifyOrganization().url)
          .formParamMap(Map(
            Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
            Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(5)
    } {
      exec { session => println("OutsideApprove" + session); session }
    }
}

object loginZoneAndSetACL{

  val masterLoginZoneAndSetACL: ScenarioBuilder = scenario("masterLoginZoneAndSetACL")
    .feed(ZoneIDFeeder.zoneIDFeed)
    .feed(OrganizationIDFeeder.organizationIDFeed)
    .feed(SellerLoginFeeder.sellerLoginFeed)
    .feed(BuyerLoginFeeder.buyerLoginFeed)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(Test.TEST_ZONE_USERNAME_UNIQUE)) }
    .doIfOrElse(session => getZoneStatus(Test.TEST_ZONE_USERNAME_UNIQUE)) {
      exec(http("VerifiedZoneLogin_GET")
        .get(routes.LoginController.loginForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .exec(http("VerifiedZoneLogin_POST")
          .post(routes.LoginController.login().url)
          .formParamMap(Map(
            Form.USERNAME -> Test.TEST_ZONE_USERNAME_UNIQUE,
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
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
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
            Form.ACL_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
            Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
            Form.ISSUE_ASSET -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT -> setACLPrivileges.issueFiat, Form.SEND_ASSET -> setACLPrivileges.sendAsset, Form.SEND_FIAT -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION -> setACLPrivileges.negotiation, Form.RELEASE_ASSET -> setACLPrivileges.releaseAsset,
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(10)
        .exec(http("SetACLBuyer_GET")
          .get(routes.SetACLController.setACLForm().url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .pause(2)
        .exec { session => session.set(Test.TEST_BUYER_ADDRESS, getAccountAddressByUsername(session(Test.TEST_BUYER_USERNAME).as[String])) }
        .exec(http("SetACLBuyer_POST")
          .post(routes.SetACLController.setACL().url)
          .formParamMap(Map(
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
            Form.ACL_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
            Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
            Form.ISSUE_ASSET -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT -> setACLPrivileges.issueFiat, Form.SEND_ASSET -> setACLPrivileges.sendAsset, Form.SEND_FIAT -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION -> setACLPrivileges.negotiation, Form.RELEASE_ASSET -> setACLPrivileges.releaseAsset,
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(5)
    } {
      exec { session => println("OutsideApprove" + session); session }
    }
}
/////////
object traderSignUp {

  val masterTraderSignUp: ScenarioBuilder = scenario("masterTraderSignUp")
    .feed(UsernameFeeder.usernameFeed)
    .feed(PasswordFeeder.passwordFeed)
    .exec { session => {
      println("*******************************************************************************************************-")
      println(session(Test.TEST_USERNAME).as[String] + "||" + session(Test.TEST_PASSWORD).as[String])
      println("*******************************************************************************************************-")
      session
    }}
    .exec(http("SignUp_GET")
      .get(routes.SignUpController.signUpForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(5)
    .exec(http("SignUp_POST")
      .post(routes.SignUpController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

}

object traderLoginAndRequestCoin {

  val masterTraderLoginAndRequestCoin: ScenarioBuilder = scenario("masterTraderLoginAndRequestCoin")
    .feed(UsernameFeeder.usernameFeed)
    .feed(PasswordFeeder.passwordFeed)
    .exec { session => {
      println("*******************************************************************************************************-")
      println(session(Test.TEST_USERNAME).as[String] + "||" + session(Test.TEST_PASSWORD).as[String])
      println("*******************************************************************************************************-")
      session
    }}
    .exec(http("Login_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(5)
    .exec(http("Login_POST")
      .post(routes.LoginController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
    .feed(CouponFeeder.couponFeed)
    .exec(http("RequestCoin_GET")
      .get(routes.SendCoinController.requestCoinsForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(5)
    .exec(http("RequestCoin_POST")
      .post(routes.SendCoinController.requestCoins().url)
      .formParamMap(Map(
        Form.COUPON -> "${%s}".format(Test.TEST_COUPON),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}

object loginMainAndApproveFaucetRequest{

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
    .pause(5)
}

object sellerLoginAndIssueAssetRequest{

  val masterSellerLoginAndIssueAssetRequest: ScenarioBuilder = scenario("masterSellerLoginAndIssueAssetRequest")
    .feed(SellerLoginFeeder.sellerLoginFeed)
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
    .feed(DocumentHashFeeder.documentHashFeed)
    .feed(AssetTypeFeeder.assetTypeFeed)
    .feed(AssetPriceFeeder.assetPriceFeed)
    .feed(QuantityUnitFeeder.quantityUnitFeed)
    .feed(AssetQuantityFeeder.assetQuantityFeed)
    .exec(http("IssueAssetRequest_GET")
      .get(routes.IssueAssetController.issueAssetRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueAssetRequest_POST")
      .post(routes.IssueAssetController.issueAssetRequest().url)
      .formParamMap(Map(
        Form.DOCUMENT_HASH -> "${%s}".format(Test.TEST_DOCUMENT_HASH),
        Form.ASSET_TYPE -> "${%s}".format(Test.TEST_ASSET_TYPE),
        Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
        Form.QUANTITY_UNIT -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}

object loginZoneAndIssueAsset{

  val masterLoginZoneAndIssueAsset: ScenarioBuilder = scenario("masterLoginZoneAndIssueAsset")
    .feed(ZoneIDFeeder.zoneIDFeed)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(Test.TEST_ZONE_USERNAME_UNIQUE)) }
    .doIfOrElse(session => getZoneStatus(Test.TEST_ZONE_USERNAME_UNIQUE)) {
      exec(http("VerifiedZoneLogin_GET")
        .get(routes.LoginController.loginForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .exec(http("VerifiedZoneLogin_POST")
          .post(routes.LoginController.login().url)
          .formParamMap(Map(
            Form.USERNAME -> Test.TEST_ZONE_USERNAME_UNIQUE,
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
            Form.NOTIFICATION_TOKEN -> "",
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(2)
        .feed(SellerLoginFeeder.sellerLoginFeed)
        .feed(BuyerLoginFeeder.buyerLoginFeed)
        .feed(DocumentHashFeeder.documentHashFeed)
        .feed(AssetTypeFeeder.assetTypeFeed)
        .feed(AssetPriceFeeder.assetPriceFeed)
        .feed(QuantityUnitFeeder.quantityUnitFeed)
        .feed(AssetQuantityFeeder.assetQuantityFeed)
        .feed(PasswordFeeder.passwordFeed)
        .feed(GasFeeder.gasFeed)
        .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForIssueAsset(session(Test.TEST_SELLER_USERNAME).as[String])) }
        .exec { session => session.set(Test.TEST_ACCOUNT_ID, session(Test.TEST_SELLER_USERNAME).as[String]) }
        .exec(http("IssueAsset_GET")
          .get(routes.IssueAssetController.issueAssetForm("${%s}".format(Test.TEST_REQUEST_ID), "${%s}".format(Test.TEST_ACCOUNT_ID), "${%s}".format(Test.TEST_DOCUMENT_HASH), "${%s}".format(Test.TEST_ASSET_TYPE), intParameters.assetPrice, "${%s}".format(Test.TEST_QUANTITY_UNIT), intParameters.assetQuantity).url)
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
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
            Form.GAS -> "${%s}".format(Test.TEST_GAS), Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(5)
    } {
      exec { session => println("OutsideApprove" + session); session }
    }
}

object buyerLoginAndIssueFiatRequest{

  val masterBuyerLoginAndIssueFiatRequest: ScenarioBuilder = scenario("masterBuyerLoginAndIssueFiatRequest")
    .feed(BuyerLoginFeeder.buyerLoginFeed)
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
    .feed(TransactionIDFeeder.transactionIDFeed)
    .feed(TransactionAmountFeeder.transactionAmountFeed)
    .exec(http("IssueFiatRequest_GET")
      .get(routes.IssueFiatController.issueFiatRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueFiatRequest_POST")
      .post(routes.IssueFiatController.issueFiatRequest().url)
      .formParamMap(Map(
        Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
        Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}

object loginZoneAndIssueFiat{

  val masterLoginZoneAndIssueFiat: ScenarioBuilder = scenario("masterLoginZoneAndIssueFiat")
    .feed(ZoneIDFeeder.zoneIDFeed)
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(Test.TEST_ZONE_USERNAME_UNIQUE)) }
    .doIfOrElse(session => getZoneStatus(Test.TEST_ZONE_USERNAME_UNIQUE)) {
      exec(http("VerifiedZoneLogin_GET")
        .get(routes.LoginController.loginForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .exec(http("VerifiedZoneLogin_POST")
          .post(routes.LoginController.login().url)
          .formParamMap(Map(
            Form.USERNAME -> Test.TEST_ZONE_USERNAME_UNIQUE,
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
            Form.NOTIFICATION_TOKEN -> "",
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(2)
        .feed(BuyerLoginFeeder.buyerLoginFeed)
        .feed(SellerLoginFeeder.sellerLoginFeed)
        .feed(TransactionIDFeeder.transactionIDFeed)
        .feed(RequestIDFeeder.requestIDFeed)
        .feed(TransactionAmountFeeder.transactionAmountFeed)
        .feed(PasswordFeeder.passwordFeed)
        .feed(GasFeeder.gasFeed)
        .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])) }
        .exec { session => session.set(Test.TEST_ACCOUNT_ID, session(Test.TEST_BUYER_USERNAME).as[String]) }
        .exec(http("IssueFiat_GET")
          .get(routes.IssueFiatController.issueFiatForm("${%s}".format(Test.TEST_REQUEST_ID), "${%s}".format(Test.TEST_ACCOUNT_ID), "${%s}".format(Test.TEST_TRANSACTION_ID), intParameters.transactionAmount).url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .pause(2)
        .exec(http("IssueFiat_POST")
          .post(routes.IssueFiatController.issueFiat().url)
          .formParamMap(Map(
            Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
            Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ACCOUNT_ID),
            Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
            Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
            Form.GAS -> "${%s}".format(Test.TEST_GAS),
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(5)
    } {
      exec { session => println("OutsideApprove" + session); session }
    }
}

object buyerLoginAndChangeBuyerBid{

  val masterBuyerLoginAndChangeBuyerBid: ScenarioBuilder = scenario("masterBuyerLoginAndChangeBuyerBid")
    .feed(BuyerLoginFeeder.buyerLoginFeed)
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
    .feed(SellerLoginFeeder.sellerLoginFeed)
    .feed(SellerAddressFeeder.sellerAddressFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(BuyerBidFeeder.buyerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_BID, session(Test.TEST_BUYER_BID).as[String]) }
    .exec { session => session.set(Test.TEST_TIME, session(Test.TEST_TIME).as[String]) }
    .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
    .exec(http("ChangeBuyerBid_GET")
      .get(routes.ChangeBuyerBidController.changeBuyerBidForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ChangeBuyerBid_POST")
      .post(routes.ChangeBuyerBidController.changeBuyerBid().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.BID -> "${%s}".format(Test.TEST_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}

object sellerLoginAndChangeSellerBid{

  val masterSellerLoginAndChangeSellerBid: ScenarioBuilder = scenario("masterSellerLoginAndChangeSellerBid")
    .feed(SellerLoginFeeder.sellerLoginFeed)
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
    .feed(BuyerLoginFeeder.buyerLoginFeed)
    .feed(BuyerAddressFeeder.buyerAddressFeed)
    .feed(SellerAddressFeeder.sellerAddressFeed)
    .feed(SellerBidFeeder.sellerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .feed(FromFeeder.fromFeed)
    .exec { session => session.set(Test.TEST_BUYER_ADDRESS, getBuyerAddress(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_BID, session(Test.TEST_SELLER_BID).as[String]) }
    .exec { session => session.set(Test.TEST_TIME, session(Test.TEST_TIME).as[String]) }
    .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
    .exec(http("ChangeSellerBid_GET")
      .get(routes.ChangeSellerBidController.changeSellerBidForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ChangeSellerBid_POST")
      .post(routes.ChangeSellerBidController.changeSellerBid().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.BID -> "${%s}".format(Test.TEST_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}


object buyerLoginAndConfirmBuyerBid{

  val masterBuyerLoginAndConfirmBuyerBid: ScenarioBuilder = scenario("masterBuyerLoginAndConfirmBuyerBid")
    .feed(BuyerLoginFeeder.buyerLoginFeed)
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
    .feed(SellerLoginFeeder.sellerLoginFeed)
    .feed(SellerAddressFeeder.sellerAddressFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(SellerBidFeeder.sellerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_BID, session(Test.TEST_SELLER_BID).as[String]) }
    .exec { session => session.set(Test.TEST_TIME, session(Test.TEST_TIME).as[String]) }
    .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
    .exec(http("ConfirmBuyerBid_GET")
      .get(routes.ConfirmBuyerBidController.confirmBuyerBidForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ConfirmBuyerBid_POST")
      .post(routes.ConfirmBuyerBidController.confirmBuyerBid().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.BID -> "${%s}".format(Test.TEST_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}


object sellerLoginAndConfirmSellerBid{

  val masterSellerLoginAndConfirmSellerBid: ScenarioBuilder = scenario("masterSellerLoginAndConfirmSellerBid")
    .feed(SellerLoginFeeder.sellerLoginFeed)
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
    .feed(BuyerLoginFeeder.buyerLoginFeed)
    .feed(BuyerAddressFeeder.buyerAddressFeed)
    .feed(SellerAddressFeeder.sellerAddressFeed)
    .feed(SellerBidFeeder.sellerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .feed(FromFeeder.fromFeed)
    .exec { session => session.set(Test.TEST_BUYER_ADDRESS, getBuyerAddress(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_BID, session(Test.TEST_SELLER_BID).as[String]) }
    .exec { session => session.set(Test.TEST_TIME, session(Test.TEST_TIME).as[String]) }
    .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
    .exec(http("ConfirmSellerBid_GET")
      .get(routes.ConfirmSellerBidController.confirmSellerBidForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ConfirmSellerBid_POST")
      .post(routes.ConfirmSellerBidController.confirmSellerBid().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.BID -> "${%s}".format(Test.TEST_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}

object loginZoneAndReleaseAsset{

  val masterLoginZoneAndReleaseAsset: ScenarioBuilder = scenario("masterLoginZoneAndReleaseAsset")
    .doIfOrElse(session => getZoneStatus(Test.TEST_ZONE_USERNAME_UNIQUE)) {
      exec(http("VerifiedZoneLogin_GET")
        .get(routes.LoginController.loginForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .exec(http("VerifiedZoneLogin_POST")
          .post(routes.LoginController.login().url)
          .formParamMap(Map(
            Form.USERNAME -> Test.TEST_ZONE_USERNAME_UNIQUE,
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
            Form.NOTIFICATION_TOKEN -> "",
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(2)
        .feed(SellerLoginFeeder.sellerLoginFeed)
        .feed(SellerAddressFeeder.sellerAddressFeed)
        .feed(PegHashFeeder.pegHashFeed)
        .feed(PasswordFeeder.passwordFeed)
        .feed(GasFeeder.gasFeed)
        .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
        .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
        .exec(http("ReleaseAsset_GET")
          .post(routes.ReleaseAssetController.releaseAssetForm().url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .pause(2)
        .exec(http("ReleaseAsset_POST")
          .post(routes.ReleaseAssetController.releaseAsset().url)
          .formParamMap(Map(
            Form.ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
            Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
            Form.GAS -> "${%s}".format(Test.TEST_GAS),
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(5)
    } {
      exec { session => println("OutsideApprove" + session); session }
    }
}

object sellerLoginAndSendAsset{

  val masterSellerLoginAndSendAsset: ScenarioBuilder = scenario("masterSellerLoginAndSendAsset")
    .feed(SellerLoginFeeder.sellerLoginFeed)
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
    .feed(SellerAddressFeeder.sellerAddressFeed)
    .feed(BuyerLoginFeeder.buyerLoginFeed)
    .feed(BuyerAddressFeeder.buyerAddressFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .exec { session => session.set(Test.TEST_BUYER_ADDRESS, getBuyerAddress(session(Test.TEST_BUYER_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
    .exec(http("SendAsset_GET")
      .get(routes.SendAssetController.sendAssetForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("SendAsset_POST")
      .post(routes.SendAssetController.sendAsset().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}


object buyerLoginAndSendFiat{

  val masterBuyerLoginAndSendFiat: ScenarioBuilder = scenario("masterBuyerLoginAndSendFiat")
    .feed(BuyerLoginFeeder.buyerLoginFeed)
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
    .feed(SellerLoginFeeder.sellerLoginFeed)
    .feed(SellerAddressFeeder.sellerAddressFeed)
    .feed(AmountFeeder.amountFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(SellerBidFeeder.sellerBidFeed)
    .feed(GasFeeder.gasFeed)
    .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
    .exec { session => session.set(Test.TEST_BID, session(Test.TEST_SELLER_BID).as[String]) }
    .exec(http("SendFiat_GET")
      .get(routes.SendFiatController.sendFiatForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("SendFiat_POST")
      .get(routes.SendFiatController.sendFiat().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.AMOUNT -> "${%s}".format(Test.TEST_AMOUNT),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}


object loginZoneAndExecuteOrder{

  val masterLoginZoneAndExecuteOrder: ScenarioBuilder = scenario("masterLoginZoneAndExecuteOrder")
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(Test.TEST_ZONE_USERNAME_UNIQUE)) }
    .doIfOrElse(session => getZoneStatus(Test.TEST_ZONE_USERNAME_UNIQUE)) {
      exec(http("VerifiedZoneLogin_GET")
        .get(routes.LoginController.loginForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .exec(http("VerifiedZoneLogin_POST")
          .post(routes.LoginController.login().url)
          .formParamMap(Map(
            Form.USERNAME -> Test.TEST_ZONE_USERNAME_UNIQUE,
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
            Form.NOTIFICATION_TOKEN -> "",
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(2)
        .feed(BuyerLoginFeeder.buyerLoginFeed)
        .feed(BuyerAddressFeeder.buyerAddressFeed)
        .feed(SellerLoginFeeder.sellerLoginFeed)
        .feed(SellerAddressFeeder.sellerAddressFeed)
        .feed(FiatProofHashFeeder.fiatProofHashFeed)
        .feed(PegHashFeeder.pegHashFeed)
        .feed(GasFeeder.gasFeed)
        .feed(AWBProofHashFeeder.awbProofHashFeed)
        .exec { session => session.set(Test.TEST_BUYER_ADDRESS, getBuyerAddress(session(Test.TEST_BUYER_USERNAME).as[String])) }
        .exec { session => session.set(Test.TEST_SELLER_ADDRESS, getSellerAddress(session(Test.TEST_SELLER_USERNAME).as[String])) }
        .exec { session => session.set(Test.TEST_PEG_HASH, getPegHashByOwnerAddress(session(Test.TEST_SELLER_ADDRESS).as[String])) }
        .exec(http("BuyerExecuteOrder_GET")
          .get(routes.BuyerExecuteOrderController.buyerExecuteOrderForm().url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .pause(2)
        .exec(http("BuyerExecuteOrder_POST")
          .get(routes.BuyerExecuteOrderController.buyerExecuteOrder().url)
          .formParamMap(Map(
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
            Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
            Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
            Form.FIAT_PROOF_HASH -> "${%s}".format(Test.TEST_FIAT_PROOF_HASH),
            Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
            Form.GAS -> "${%s}".format(Test.TEST_GAS),
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(10)
        .feed(SellerLoginFeeder.sellerLoginFeed)
        .exec(http("SellerExecuteOrder_GET")
          .get(routes.SellerExecuteOrderController.sellerExecuteOrderForm().url)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .pause(2)
        .exec(http("SellerExecuteOrder_POST")
          .get(routes.SellerExecuteOrderController.sellerExecuteOrder().url)
          .formParamMap(Map(
            Form.PASSWORD -> Test.TEST_ZONE_PASSWORD_UNIQUE,
            Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
            Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
            Form.AWB_PROOF_HASH -> "${%s}".format(Test.TEST_AWB_PROOF_HASH),
            Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
            Form.GAS -> "${%s}".format(Test.TEST_GAS),
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
        .pause(5)
    } {
      exec { session => println("OutsideApprove" + session); session }
    }
}
