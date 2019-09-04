//package controllersTest
//
//import constants.{Form, Test}
//import controllers.routes
//import feeders._
//import io.gatling.core.Predef._
//import io.gatling.core.structure.ScenarioBuilder
//import io.gatling.http.Predef._
//
//class BuyerExecuteOrderControllerTest extends Simulation {
//
//  val scenarioBuilder: ScenarioBuilder = buyerExecuteOrderControllerTest.buyerExecuteOrderScenario
//  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
//}
//
//object buyerExecuteOrderControllerTest {
//
//  val buyerExecuteOrderScenario: ScenarioBuilder = scenario("BuyerExecuteOrder")
//    .feed(PasswordFeeder.passwordFeed)
//    .feed(SellerAddressFeeder.sellerAddressFeed)
//    .feed(BuyerAddressFeeder.buyerAddressFeed)
//    .feed(FiatProofHashFeeder.fiatProofHashFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(GasFeeder.gasFeed)
//    .feed(FromFeeder.fromFeed)
//    .exec(http("BuyerExecuteOrder_GET")
//      .get(routes.BuyerExecuteOrderController.buyerExecuteOrderForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("BuyerExecuteOrder_POST")
//      .post(routes.BuyerExecuteOrderController.buyerExecuteOrder().url)
//      .formParamMap(Map(
//        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
//        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
//        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
//        Form.FIAT_PROOF_HASH -> "${%s}".format(Test.TEST_FIAT_PROOF_HASH),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//
//  val blockchainBuyerExecuteOrderScenario: ScenarioBuilder = scenario("BlockchainBuyerExecuteOrder")
//    .feed(FromFeeder.fromFeed)
//    .feed(PasswordFeeder.passwordFeed)
//    .feed(SellerAddressFeeder.sellerAddressFeed)
//    .feed(BuyerAddressFeeder.buyerAddressFeed)
//    .feed(FiatProofHashFeeder.fiatProofHashFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec(http("BlockchainBuyerExecuteOrder_GET")
//      .get(routes.BuyerExecuteOrderController.blockchainBuyerExecuteOrderForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("BlockchainBuyerExecuteOrder_POST")
//      .post(routes.BuyerExecuteOrderController.blockchainBuyerExecuteOrder().url)
//      .formParamMap(Map(
//        Form.FROM -> "${%s}".format(Test.TEST_FROM),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
//        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
//        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
//        Form.FIAT_PROOF_HASH -> "${%s}".format(Test.TEST_FIAT_PROOF_HASH),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//}