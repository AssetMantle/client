package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class BuyerExecuteOrderControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = buyerExecuteOrderControllerTest.blockchainBuyerExecuteOrderScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object buyerExecuteOrderControllerTest {

  val moderatedBuyerExecuteOrderScenario: ScenarioBuilder = scenario("ModeratedBuyerExecuteOrder")

    .exec(http("Moderated_Buyer_Execute_Order_Document_Upload_Form_GET")
      .get(session=>routes.BuyerExecuteOrderController.moderatedBuyerExecuteOrderDocument(session(Test.TEST_BUYER_ADDRESS).as[String],session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
        .check(bodyString.saveAs("buyerExecute"))
      //.check(substring("FIAT_PROOF").exists)
    )
    .exec{session=> println(session)
    session}
    .pause(1)
    .exec(http("Zone_Upload_Negotiation_FIAT_PROOF_Form")
      .get(session=> routes.FileController.uploadZoneNegotiationForm("FIAT_PROOF",session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
      .check(substring("BROWSE").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder3.imageFeed3)
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
        Form.PASSWORD ->  "${%s}".format(Test.TEST_ZONE_PASSWORD)
      ))
      .check(substring("SUCCESS BUYER_ORDER_EXECUTED").exists)
    )
    .pause(3)

  val unmoderatedBuyerExecuteOrderScenario: ScenarioBuilder = scenario("UnmoderatedBuyerExecuteOrderScenario")

    .exec(http("Unmoderated_Buyer_Execute_Order_Document_Upload_Form_GET")
      .get(session=>routes.BuyerExecuteOrderController.buyerExecuteOrderDocument(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
      .check(substring("FIAT_PROOF").exists)
    )
    .pause(1)
    .exec(http("Trader_Upload_Negotiation_FIAT_PROOF")
      .get(session=> routes.FileController.uploadTraderNegotiationForm("FIAT_PROOF",session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
      .check(substring("BROWSE").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder3.imageFeed3)
    .exec(http("Trader_Upload_Negotiation_FIAT_PROOF")
      .post(routes.FileController.uploadTraderNegotiation("FIAT_PROOF").url)
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
        .get(session=>routes.FileController.storeTraderNegotiation(session(Test.TEST_FILE_NAME).as[String],"FIAT_PROOF",session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
    )
    .pause(2)
    .exec(http("Buyer_Execute_Form_Get")
      .get(session=> routes.BuyerExecuteOrderController.buyerExecuteOrderForm(session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.BUYER_EXECUTE_ORDER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.FIAT_PROOF_HASH), "value").saveAs(Form.FIAT_PROOF_HASH))
    )
    .pause(2)
    .exec(http("Buyer_Execute_POST")
      .post(routes.BuyerExecuteOrderController.buyerExecuteOrder().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.FIAT_PROOF_HASH -> "${%s}".format(Form.FIAT_PROOF_HASH),
        Form.PEG_HASH ->  "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_BUYER_PASSWORD)
      ))
      .check(substring("SUCCESS BUYER_ORDER_EXECUTED").exists)
    )
    .pause(3)

  val blockchainBuyerExecuteOrderScenario: ScenarioBuilder = scenario("BlockchainBuyerExecuteOrder")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(SellerAddressFeeder.sellerAddressFeed)
    .feed(BuyerAddressFeeder.buyerAddressFeed)
    .feed(FiatProofHashFeeder.fiatProofHashFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(ModeFeeder.modeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainBuyerExecuteOrder_GET")
      .get(routes.BuyerExecuteOrderController.blockchainBuyerExecuteOrderForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_BUYER_EXECUTE_ORDER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainBuyerExecuteOrder_POST")
      .post(routes.BuyerExecuteOrderController.blockchainBuyerExecuteOrder().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.FIAT_PROOF_HASH -> "${%s}".format(Test.TEST_FIAT_PROOF_HASH),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      check(substring("SUCCESS BUYER_ORDER_EXECUTED").exists)
    )
}
