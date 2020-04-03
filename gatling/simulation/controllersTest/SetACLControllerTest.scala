package controllersTest

import constants.{Form, Test}
import controllers.routes

import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

class SetACLControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = setACLControllerTest.addTraderRequest
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object setACLPrivileges {
  val issueAsset = true
  val issueFiat = true
  val sendAsset = true
  val sendFiat = true
  val redeemAsset = true
  val redeemFiat = true
  val sellerExecuteOrder = true
  val buyerExecuteOrder = true
  val changeBuyerBid = true
  val changeSellerBid = true
  val confirmBuyerBid = true
  val confirmSellerBid = true
  val negotiation = true
  val releaseAsset = true
}

object setACLControllerTest {

  val traderKYCs=constants.File.TRADER_KYC_DOCUMENT_TYPES

  val addTraderRequest=scenario("AddTraderRequest")

    .exec(http("Add_Trader_Form_GET")
      .get(routes.SetACLController.addTraderForm().url)
      .check(css("legend:contains(%s)".format("Add Trader")).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(NameFeeder.nameFeed)
    .exec(http("Add_Trader_POST")
            .post(routes.SetACLController.addTrader().url)
            .formParamMap(Map(
              Form.CSRF_TOKEN-> "${%s}".format(Form.CSRF_TOKEN),
              Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
            ))
      .check(substring("TRADER_KYC_FILES").exists)
    )
    .pause(2)
    .foreach(traderKYCs,"documentType"){
      feed(ImageFeeder.imageFeed)
        .exec(http("TraderKYC_Upload_"+"${documentType}"+"_Form")
          .get(session=>routes.SetACLController.userUploadTraderKYCForm(session("documentType").as[String]).url)
          .check(substring("BROWSE").exists)
          .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
        )
        .pause(3)
        .exec(http("TraderKYC_Upload_"+"${documentType}")
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
          http("Store_TraderKYC_"+"${documentType}")
            .get(session=>routes.SetACLController.userStoreTraderKYC(session(Test.TEST_FILE_NAME).as[String],session("documentType").as[String]).url)
            .check(substring("TRADER_KYC_FILES").exists)
        )
        .pause(2)
    }
    .exec(http("User_Review_Add_Trader_Request_Form")
      .get(routes.SetACLController.userReviewAddTraderRequestForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.REVIEW_ADD_TRADER_ON_COMPLETION.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("User_Review_Add_Trader_Request")
      .post(routes.SetACLController.userReviewAddTraderRequest().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.COMPLETION -> true
      ))
      .check(substring("Trader Added For Verification").exists)
    )
    .pause(3)

  val zoneVerifyTrader=scenario("zoneVerifyTrader")
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
    .pause(1)
    .exec(http("Zone_Verify_Trader_Form_GET")
      .get(session=>routes.SetACLController.zoneVerifyTraderForm(session(Test.TEST_TRADER_USERNAME).as[String],session(Test.TEST_ORGANIZATION_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.ZONE_VERIFY_TRADER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Zone_Verify_Trader_POST")
      .post(routes.SetACLController.zoneVerifyTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_ID-> "${%s}".format(Test.TEST_TRADER_USERNAME),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ISSUE_ASSET_ACL -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT_ACL -> setACLPrivileges.issueFiat, Form.SEND_ASSET_ACL -> setACLPrivileges.sendAsset, Form.SEND_FIAT_ACL -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET_ACL -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT_ACL -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER_ACL -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER_ACL -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID_ACL -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID_ACL -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID_ACL -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID_ACL -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION_ACL -> setACLPrivileges.negotiation, Form.RELEASE_ASSET_ACL -> setACLPrivileges.releaseAsset,
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD)
      ))
      .check(substring("ALL_KYC_FILES_NOT_VERIFIED").exists)
      .check(status.is(412))
    )
    .pause(2)


  val organizationVerifyTrader=scenario("organizationVerifyTrader")
    .exec(http("Get_Organization_Pending_Verify_Trader_Request")
      .get(routes.SetACLController.organizationViewVerifyTraderRequests().url)
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
    .pause(2)
    .exec(http("Organization_Verify_Trader_GET")
      .get(session=>routes.SetACLController.organizationVerifyTraderForm(session(Test.TEST_TRADER_USERNAME).as[String],session(Test.TEST_ORGANIZATION_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.ORGANIZATION_VERIFY_TRADER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Organization_Verify_Trader_POST")
      .post(routes.SetACLController.organizationVerifyTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_ID-> "${%s}".format(Test.TEST_TRADER_USERNAME),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ISSUE_ASSET_ACL -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT_ACL -> setACLPrivileges.issueFiat, Form.SEND_ASSET_ACL -> setACLPrivileges.sendAsset, Form.SEND_FIAT_ACL -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET_ACL -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT_ACL -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER_ACL -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER_ACL -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID_ACL -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID_ACL -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID_ACL -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID_ACL -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION_ACL -> setACLPrivileges.negotiation, Form.RELEASE_ASSET_ACL -> setACLPrivileges.releaseAsset,
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_ORGANIZATION_PASSWORD)
      ))
      .check(substring("Acl Set").exists)
    )
    .pause(2)


  val verifyTraderAndSetACLScenario= scenario("verifyTraderAndSetACL")
    .exec{session=> session.set(Test.TEST_USERNAME,"testZone1512").set(Test.TEST_PASSWORD,"qwerty1234567890")}
    .exec(loginControllerTest.loginScenario)
    .exec(session=> session.set(Test.TEST_TRADER_ID,"ED5378E4C781B4C7"))
    .exec(http("Get_Zone_Pending_Verify_Trader_Request")
      .get(routes.SetACLController.zoneViewPendingVerifyTraderRequests().url)
    )
    .foreach(traderKYCs,"documentType"){
      exec(http("Zone_Trader_KYC_update_Status"+"${documentType}")
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
    .exec(session=> session.set(Test.TEST_ACCOUNT_ID,"testTrader1514"))
    .exec(http("ZoneVerifyTrader_GET")
        .get(routes.SetACLController.zoneVerifyTraderForm("${%s}".format(Test.TEST_ACCOUNT_ID),"8875DC578AB26CCB").url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("ZoneVerifyTrader_POST")
      .post(routes.SetACLController.zoneVerifyTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_ID-> "${%s}".format(Test.TEST_ACCOUNT_ID),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ISSUE_ASSET_ACL -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT_ACL -> setACLPrivileges.issueFiat, Form.SEND_ASSET_ACL -> setACLPrivileges.sendAsset, Form.SEND_FIAT_ACL -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET_ACL -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT_ACL -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER_ACL -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER_ACL -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID_ACL -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID_ACL -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID_ACL -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID_ACL -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION_ACL -> setACLPrivileges.negotiation, Form.RELEASE_ASSET_ACL -> setACLPrivileges.releaseAsset,
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "qwerty1234567890"
      )))
    .exec{session=> session.set(Test.TEST_USERNAME,"testOrg1512").set(Test.TEST_PASSWORD,"qwerty1234567890")}
    .exec(loginControllerTest.loginScenario)
    .exec(http("Get_Organization_Pending_Verify_Trader_Request")
      .get(routes.SetACLController.organizationViewVerifyTraderRequests().url)
    )
    .foreach(traderKYCs,"documentType"){
      exec(http("Organization_Trader_KYC_update_Status"+"${documentType}")
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
      .get(routes.SetACLController.organizationVerifyTraderForm("${%s}".format(Test.TEST_ACCOUNT_ID),"8875DC578AB26CCB").url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(session=>session.set(Test.TEST_ISSUE_ASSET,true))
    .exec(http("ZoneVerifyTrader_POST")
      .post(routes.SetACLController.organizationVerifyTrader().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.ACCOUNT_ID-> "${%s}".format(Test.TEST_ACCOUNT_ID),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ISSUE_ASSET_ACL -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT_ACL -> setACLPrivileges.issueFiat, Form.SEND_ASSET_ACL -> setACLPrivileges.sendAsset, Form.SEND_FIAT_ACL -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET_ACL -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT_ACL -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER_ACL -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER_ACL -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID_ACL -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID_ACL -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID_ACL -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID_ACL -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION_ACL -> setACLPrivileges.negotiation, Form.RELEASE_ASSET_ACL -> setACLPrivileges.releaseAsset,
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "qwerty1234567890"
      )))

  val blockchainSetACLScenario: ScenarioBuilder = scenario("BlockchainSetACL")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ACLAddressFeeder.aclAddressFeed)
    .feed(OrganizationIDFeeder.organizationIDFeed)
    .feed(GasFeeder.gasFeed)
    .feed(ModeFeeder.modeFeed)
    .exec(http("BlockchainSetACL_GET")
      .get(routes.SetACLController.blockchainSetACLForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_SET_ACL.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainSetACL_POST")
      .post(routes.SetACLController.blockchainSetACL().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.ACL_ADDRESS -> "${%s}".format(Test.TEST_ACL_ADDRESS),
        Form.ORGANIZATION_ID -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        Form.ZONE_ID-> "${%s}".format(Test.TEST_ZONE_ID),
        Form.ISSUE_ASSET_ACL -> setACLPrivileges.issueAsset, Form.ISSUE_FIAT_ACL -> setACLPrivileges.issueFiat, Form.SEND_ASSET_ACL -> setACLPrivileges.sendAsset, Form.SEND_FIAT_ACL -> setACLPrivileges.sendFiat, Form.REDEEM_ASSET_ACL -> setACLPrivileges.redeemAsset, Form.REDEEM_FIAT_ACL -> setACLPrivileges.redeemFiat, Form.SELLER_EXECUTE_ORDER_ACL -> setACLPrivileges.sellerExecuteOrder, Form.BUYER_EXECUTE_ORDER_ACL -> setACLPrivileges.buyerExecuteOrder, Form.CHANGE_BUYER_BID_ACL -> setACLPrivileges.changeBuyerBid, Form.CHANGE_SELLER_BID_ACL -> setACLPrivileges.changeSellerBid, Form.CONFIRM_BUYER_BID_ACL -> setACLPrivileges.confirmBuyerBid, Form.CONFIRM_SELLER_BID_ACL -> setACLPrivileges.confirmSellerBid, Form.NEGOTIATION_ACL -> setACLPrivileges.negotiation, Form.RELEASE_ASSET_ACL -> setACLPrivileges.releaseAsset,
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ACL_SET").exists)
    )

  def getAccountAddressByUsername(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "accountAddress" FROM master."Account" WHERE "id" = '$query'),'0') AS "accountAddress";""")
    sqlQueryFeeder.apply().next()("accountAddress").toString
  }

  def getTraderID(query: String): String={
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master."Trader" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }
}
