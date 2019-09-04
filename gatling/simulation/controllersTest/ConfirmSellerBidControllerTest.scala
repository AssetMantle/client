//package controllersTest
//
//import constants.{Form, Test}
//import controllers.routes
//import feeders._
//import io.gatling.core.Predef._
//import io.gatling.core.structure.ScenarioBuilder
//import io.gatling.http.Predef._
//
//class ConfirmSellerBidControllerTest extends Simulation {
//
//  val scenarioBuilder: ScenarioBuilder = confirmSellerBidControllerTest.confirmSellerBidScenario
//  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
//}
//
//object confirmSellerBidControllerTest {
//
//  val confirmSellerBidScenario: ScenarioBuilder = scenario("ConfirmSellerBid")
//    .feed(PasswordFeeder.passwordFeed)
//    .feed(ToFeeder.toFeed)
//    .feed(SellerBidFeeder.sellerBidFeed)
//    .feed(TimeFeeder.timeFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec(http("ConfirmSellerBid_GET")
//      .get(routes.ConfirmSellerBidController.confirmSellerBidForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("ConfirmSellerBid_POST")
//      .post(routes.ConfirmSellerBidController.confirmSellerBid().url)
//      .formParamMap(Map(
//        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
//        Form.TO -> "${%s}".format(Test.TEST_TO),
//        Form.BID -> "${%s}".format(Test.TEST_BID),
//        Form.TIME -> "${%s}".format(Test.TEST_TIME),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//
//  val blockchainConfirmSellerBidScenario: ScenarioBuilder = scenario("BlockchainConfirmSellerBid")
//    .feed(FromFeeder.fromFeed)
//    .feed(PasswordFeeder.passwordFeed)
//    .feed(ToFeeder.toFeed)
//    .feed(SellerBidFeeder.sellerBidFeed)
//    .feed(TimeFeeder.timeFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec(http("BlockchainConfirmSellerBid_GET")
//      .get(routes.ConfirmSellerBidController.blockchainConfirmSellerBidForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("BlockchainConfirmSellerBid_POST")
//      .post(routes.ConfirmSellerBidController.blockchainConfirmSellerBid().url)
//      .formParamMap(Map(
//        Form.FROM -> "${%s}".format(Test.TEST_FROM),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
//        Form.TO -> "${%s}".format(Test.TEST_TO),
//        Form.BID -> "${%s}".format(Test.TEST_BID),
//        Form.TIME -> "${%s}".format(Test.TEST_TIME),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//}