package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SellerExecuteOrderControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = sellerExecuteOrderControllerTest.moderatedSellerExecuteOrder
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object sellerExecuteOrderControllerTest {

  val moderatedSellerExecuteOrder=scenario("ModeratedSellerExecuteOrder")

    .exec(http("Moderated_Seller_Execute_Order_Document_Upload_Form_GET")
      .get(session=>routes.SellerExecuteOrderController.moderatedSellerExecuteOrderDocument(session(Test.TEST_BUYER_ADDRESS).as[String],session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(substring("AWB_PROOF").exists)
    )
    .pause(1)
    .exec(http("Zone_Upload_Negotiation_AWB_PROOF")
      .get(session=> routes.FileController.uploadZoneNegotiationForm("AWB_PROOF",session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
      .check(substring("BROWSE").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder4.imageFeed4)
    .feed(GasFeeder.gasFeed)
    .exec(http("Zone_Upload_Negotiation_AWB_Proof")
        .post(routes.FileController.uploadZoneNegotiation("AWB_PROOF").url)
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
      http("Store_Negotiation_Document_AWBProof")
        .get(session=>routes.FileController.storeZoneNegotiation(session(Test.TEST_FILE_NAME).as[String],"AWB_PROOF",session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
    )
    .pause(2)
    .exec(http("Moderated_Seller_Execute_Form_GET")
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
          Form.PASSWORD -> "${%s}".format(Test.TEST_ZONE_PASSWORD)
        ))
      .check(substring("SUCCESS SELLER_ORDER_EXECUTED").exists)
    )
    .pause(3)

  val unmoderatedSellerExecuteOrder=scenario("UnmoderatedSellerExecuteOrder")

    .exec(http("Seller_Execute_Order_Document_Upload_Form_GET")
      .get(session=>routes.SellerExecuteOrderController.sellerExecuteOrderDocument(session(Test.TEST_NEGOTIATION_ID).as[String]).url)
     // .check(substring("AWB_PROOF").exists)
    )
    .pause(1)
    .exec(http("Trader_Upload_Negotiation_AWB_PROOF")
      .get(session=> routes.FileController.uploadTraderNegotiationForm("AWB_PROOF",session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
      .check(substring("BROWSE").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .pause(2)
    .feed(ImageFeeder4.imageFeed4)
    .feed(GasFeeder.gasFeed)
    .exec(http("Trader_Upload_Negotiation_AWB_Proof")
      .post(routes.FileController.uploadTraderNegotiation("AWB_PROOF").url)
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
      http("Store_Negotiation_Document_AWBProof")
        .get(session=>routes.FileController.storeTraderNegotiation(session(Test.TEST_FILE_NAME).as[String],"AWB_PROOF",session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
    )
    .pause(2)
    .exec(http("Seller_Execute_Form_GET")
      .get(session=> routes.SellerExecuteOrderController.sellerExecuteOrderForm(session(Test.TEST_NEGOTIATION_REQUEST_ID).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.SELLER_EXECUTE_ORDER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.AWB_PROOF_HASH), "value").saveAs(Form.AWB_PROOF_HASH))
    )
    .pause(2)
    .exec(http("Seller_Execute_POST")
      .post(routes.SellerExecuteOrderController.sellerExecuteOrder().url)
      .formParamMap(Map(
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.AWB_PROOF_HASH -> "${%s}".format(Form.AWB_PROOF_HASH),
        Form.PEG_HASH ->  "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD)
      ))
      .check(substring("SUCCESS SELLER_ORDER_EXECUTED").exists)
    )
    .pause(3)



  /*val sellerExecuteOrderScenario: ScenarioBuilder = scenario("SellerExecuteOrder")
    .feed(PasswordFeeder.passwordFeed)
    .feed(BuyerAddressFeeder.buyerAddressFeed)
    .feed(SellerAddressFeeder.sellerAddressFeed)
    .feed(AWBProofHashFeeder.awbProofHashFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("SellerExecuteOrder_GET")
      .get(routes.SellerExecuteOrderController.sellerExecuteOrderForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("SellerExecuteOrder_POST")
      .post(routes.SellerExecuteOrderController.sellerExecuteOrder().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.AWB_PROOF_HASH -> "${%s}".format(Test.TEST_AWB_PROOF_HASH),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
*/
  val blockchainSellerExecuteOrderScenario: ScenarioBuilder = scenario("BlockchainSellerExecuteOrder")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(BuyerAddressFeeder.buyerAddressFeed)
    .feed(SellerAddressFeeder.sellerAddressFeed)
    .feed(AWBProofHashFeeder.awbProofHashFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(ModeFeeder.modeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainSellerExecuteOrder_GET")
      .get(routes.SellerExecuteOrderController.blockchainSellerExecuteOrderForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_SELLER_EXECUTE_ORDER.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainSellerExecuteOrder_POST")
      .post(routes.SellerExecuteOrderController.blockchainSellerExecuteOrder().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.AWB_PROOF_HASH -> "${%s}".format(Test.TEST_AWB_PROOF_HASH),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS SELLER_ORDER_EXECUTED").exists)
    )
}