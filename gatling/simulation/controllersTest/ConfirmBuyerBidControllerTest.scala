/*
package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

class ConfirmBuyerBidControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = confirmBuyerBidControllerTest.blockchainConfirmBuyerBidScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object confirmBuyerBidControllerTest {

  val confirmBuyerBidScenario: ScenarioBuilder = scenario("ConfirmBuyerBid")
    .feed(ConfirmBidFeeder.confirmBidFeed)
    .exec(http("Confirm_Buyer_Bid_Detail_GET")
      .get(session=>routes.ConfirmBuyerBidController.confirmBuyerBidDetailForm(session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String],950).url)
      .check(css("legend:contains(%s)".format(constants.Form.CONFIRM_BUYER_BID_DETAIL.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.REQUEST_ID), "value").saveAs(Test.TEST_REQUEST_ID))
    )
    .pause(2)
    .exec(http("Confirm_Buyer_Bid_Detail_POST")
      .post(routes.ConfirmBuyerBidController.confirmBuyerBidDetail().url)
      .formParamMap(Map(
        Form.REQUEST_ID-> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.BID -> "${%s}".format(Test.TEST_CONFIRM_BID),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("UPLOAD BUYER_CONTRACT").exists)
    )
    .pause(2)
  .feed(ImageFeeder.imageFeed)
    .exec(http("Trader_Upload_BuyerContract_Negotiation_Form")
      .get(session=> routes.FileController.uploadTraderNegotiationForm("BUYER_CONTRACT",session(Test.TEST_REQUEST_ID).as[String]).url)
      .check(substring("Browse").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("Trader_Upload_Buyer_Contract")
      .post(routes.FileController.uploadTraderNegotiation("BUYER_CONTRACT").url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(http("Store_Negotiation_Buyer_Contract_Hash")
        .get(session=> routes.FileController.storeTraderNegotiation(session(Test.TEST_FILE_NAME).as[String],"BUYER_CONTRACT",session(Test.TEST_REQUEST_ID).as[String]).url)
    )
    .pause(2)
    .exec(http("Confirm_Buyer_Bid_Form_GET")
      .get(session=> routes.ConfirmBuyerBidController.confirmBuyerBidForm(session(Test.TEST_REQUEST_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.CONFIRM_BUYER_BID.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("Confirm_Buyer_Bid_POST")
        .post(routes.ConfirmBuyerBidController.confirmBuyerBid().url)
        .formParamMap(Map(
          Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
          Form.REQUEST_ID-> "${%s}".format(Test.TEST_REQUEST_ID),
          Form.TIME -> "${%s}".format(Test.TEST_TIME),
          Form.GAS -> "${%s}".format(Test.TEST_GAS),
          Test.PASSWORD ->"${%s}".format(Test.TEST_BUYER_PASSWORD)
        ))
      .check(substring("SUCCESS BUYER_BID_CONFIRMED").exists)
    )

  val blockchainConfirmBuyerBidScenario: ScenarioBuilder = scenario("BlockchainConfirmBuyerBid")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(BuyerBidFeeder.buyerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(ModeFeeder.modeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainConfirmBuyerBid_GET")
      .get(routes.ConfirmBuyerBidController.blockchainConfirmBuyerBidForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_CONFIRM_BUYER_BID.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainConfirmBuyerBid_POST")
      .post(routes.ConfirmBuyerBidController.blockchainConfirmBuyerBid().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Test.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.BID -> "${%s}".format(Test.TEST_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("SUCCESS BUYER_BID_CONFIRMED").exists)
    )

  def getNegotiationRequestIDFromSellerAccountID(sellerUsername: String) = {
    println("getNegotiationIDFromSellerAccountID-------------------"+sellerUsername)
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://18.136.170.155:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master_transaction."NegotiationRequest" WHERE "sellerAccountID" = '$sellerUsername'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getNegotiationIDFromSellerAccountID(sellerUsername: String) = {
    println("getNegotiationIDFromSellerAccountID-------------------"+sellerUsername)
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://18.136.170.155:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "negotiationID" FROM master_transaction."NegotiationRequest" WHERE "sellerAccountID" = '$sellerUsername'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }
}*/
