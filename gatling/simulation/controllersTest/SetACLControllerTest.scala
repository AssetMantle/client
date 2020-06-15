package controllersTest

import constants.{Form, Test}
import controllers.routes

import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

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

  val addTraderRequest = scenario("AddTraderRequest")
    .exec(http("Add_Trader_Form_GET")
      .get(routes.SetACLController.addTraderForm().url)
      .check(css("legend:contains(Register Trader)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("Add_Trader_POST")
      .post(routes.SetACLController.addTrader().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.ORGANIZATION_ID.name -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
      ))
      .check(substring("Details submitted for organization approval").exists)
    )
    .pause(2)

  val organizationVerifyTrader = scenario("organizationVerifyTrader")
    .exec(http("Organization_Verify_Trader_GET")
      .get(session => routes.SetACLController.organizationVerifyTraderForm(session(Test.TEST_TRADER_ID).as[String]).url)
      .check(css("legend:contains(Set Trader Controls)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .feed(GasFeeder.gasFeed)
    .exec(http("Organization_Verify_Trader_POST")
      .post(routes.SetACLController.organizationVerifyTrader().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        constants.FormField.ACCOUNT_ID.name -> "${%s}".format(Test.TEST_TRADER_USERNAME),
        constants.FormField.ORGANIZATION_ID.name -> "${%s}".format(Test.TEST_ORGANIZATION_ID),
        constants.FormField.ISSUE_ASSET.name -> setACLPrivileges.issueAsset,
        constants.FormField.ISSUE_FIAT.name -> setACLPrivileges.issueFiat,
        constants.FormField.SEND_ASSET.name -> setACLPrivileges.sendAsset,
        constants.FormField.SEND_FIAT.name -> setACLPrivileges.sendFiat,
        constants.FormField.REDEEM_ASSET.name -> setACLPrivileges.redeemAsset,
        constants.FormField.REDEEM_FIAT.name -> setACLPrivileges.redeemFiat,
        constants.FormField.SELLER_EXECUTE_ORDER.name -> setACLPrivileges.sellerExecuteOrder,
        constants.FormField.BUYER_EXECUTE_ORDER.name -> setACLPrivileges.buyerExecuteOrder,
        constants.FormField.CHANGE_BUYER_BID.name -> setACLPrivileges.changeBuyerBid,
        constants.FormField.CHANGE_SELLER_BID.name -> setACLPrivileges.changeSellerBid,
        constants.FormField.CONFIRM_BUYER_BID.name -> setACLPrivileges.confirmBuyerBid,
        constants.FormField.CONFIRM_SELLER_BID.name -> setACLPrivileges.confirmSellerBid,
        constants.FormField.NEGOTIATION.name -> setACLPrivileges.negotiation,
        constants.FormField.RELEASE_ASSET.name -> setACLPrivileges.releaseAsset,
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        Test.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD)
      ))
      .check(substring("Trader Controls set successfully").exists)
    )
    .pause(2)

  def getTraderID(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master."Trader" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }
}
