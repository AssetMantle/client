package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SellerExecuteOrderControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = sellerExecuteOrderControllerTest.sellerExecuteOrderScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object sellerExecuteOrderControllerTest {

  val sellerExecuteOrderScenario: ScenarioBuilder = scenario("SellerExecuteOrder")
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
    .pause(5)

  val blockchainSellerExecuteOrderScenario: ScenarioBuilder = scenario("BlockchainSellerExecuteOrder")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(BuyerAddressFeeder.buyerAddressFeed)
    .feed(SellerAddressFeeder.sellerAddressFeed)
    .feed(AWBProofHashFeeder.awbProofHashFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainSellerExecuteOrder_GET")
      .get(routes.SellerExecuteOrderController.blockchainSellerExecuteOrderForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainSellerExecuteOrder_POST")
      .post(routes.SellerExecuteOrderController.blockchainSellerExecuteOrder().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.AWB_PROOF_HASH -> "${%s}".format(Test.TEST_AWB_PROOF_HASH),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}