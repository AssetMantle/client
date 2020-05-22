package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class ConfirmSellerBidControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = confirmSellerBidControllerTest.confirmSellerBidScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object confirmSellerBidControllerTest {

  val confirmSellerBidScenario: ScenarioBuilder = scenario("ConfirmSellerBid")

    .feed(ConfirmBidFeeder.confirmBidFeed)
    .exec(http("Confirm_Seller_Bid_Detail_GET")
      .get(session=> routes.ConfirmSellerBidController.confirmSellerBidDetailForm(session(Test.TEST_BUYER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String],950).url)
      .check(css("legend:contains(%s)".format(constants.Form.CONFIRM_SELLER_BID_DETAIL.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.REQUEST_ID), "value").saveAs(Test.TEST_REQUEST_ID))
    )
    .pause(2)
    .exec(http("Confirm_Seller_Bid_Detail_POST")
      .post(routes.ConfirmSellerBidController.confirmSellerBidDetail().url)
      .formParamMap(Map(
        Form.REQUEST_ID-> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.BID -> "${%s}".format(Test.TEST_CONFIRM_BID),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("UPLOAD SELLER_CONTRACT").exists)
    )
    .pause(2)
    .feed(ImageFeeder2.imageFeed2)
    .exec(http("Trader_Upload_Seller_Contract_Negotiation_Form")
      .get(session=>routes.FileController.uploadTraderNegotiationForm("SELLER_CONTRACT",session(Test.TEST_REQUEST_ID).as[String]).url)
      .check(substring("BROWSE").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("Trader_Upload_Seller_Contract")
      .post(routes.FileController.uploadTraderNegotiation("SELLER_CONTRACT").url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.RESUMABLE_CHUNK_NUMBER -> "1",
        Form.RESUMABLE_CHUNK_SIZE  -> "1048576",
        Form.RESUMABLE_TOTAL_SIZE -> "${%s}".format(Test.TEST_FILE_SIZE),
        Form.RESUMABLE_IDENTIFIER -> "document",
        Form.RESUMABLE_FILE_NAME -> "${%s}".format(Test.TEST_FILE_NAME)))
      .bodyPart(RawFileBodyPart("file", Test.IMAGE_FILE_FEED+"${%s}".format(Test.TEST_FILE_NAME))
        .transferEncoding("binary")).asMultipartForm)
    .exec(
      http("Store_Negotiation_Seller_Contract_Hash")
        .get(session=> routes.FileController.storeTraderNegotiation(session(Test.TEST_FILE_NAME).as[String],"SELLER_CONTRACT",session(Test.TEST_REQUEST_ID).as[String]).url)
    )
    .pause(2)
    .exec(http("Confirm_Seller_Bid_Form_GET")
      .get(session=> routes.ConfirmSellerBidController.confirmSellerBidForm(session(Test.TEST_REQUEST_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.CONFIRM_SELLER_BID.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(1)
    .exec(http("Confirm_Seller_Bid_POST")
      .post(routes.ConfirmSellerBidController.confirmSellerBid().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.REQUEST_ID-> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Test.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD)
      ))
      .check(substring("SUCCESS SELLER_BID_CONFIRMED").exists)
    )
    .pause(3)


  val blockchainConfirmSellerBidScenario: ScenarioBuilder = scenario("BlockchainConfirmSellerBid")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(SellerBidFeeder.sellerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(ModeFeeder.modeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainConfirmSellerBid_GET")
      .get(routes.ConfirmSellerBidController.blockchainConfirmSellerBidForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_CONFIRM_SELLER_BID.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainConfirmSellerBid_POST")
      .post(routes.ConfirmSellerBidController.blockchainConfirmSellerBid().url)
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
      .check(substring("SUCCESS SELLER_BID_CONFIRMED").exists)
    )
}