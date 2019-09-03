//package controllersTest
//
//import constants.{Form, Test}
//import controllers.routes
//import feeders._
//import io.gatling.core.Predef._
//import io.gatling.core.structure.ScenarioBuilder
//import io.gatling.http.Predef._
//
//class SendAssetControllerTest extends Simulation {
//
//  val scenarioBuilder: ScenarioBuilder = sendAssetControllerTest.sendAssetScenario
//  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
//}
//
//object sendAssetControllerTest {
//
//  val sendAssetScenario: ScenarioBuilder = scenario("SendAsset")
//    .feed(PasswordFeeder.passwordFeed)
//    .feed(ToFeeder.toFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec(http("SendAsset_GET")
//      .get(routes.SendAssetController.sendAssetForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("SendAsset_POST")
//      .post(routes.SendAssetController.sendAsset().url)
//      .formParamMap(Map(
//        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
//        Form.TO -> "${%s}".format(Test.TEST_TO),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//
//  val blockchainSendAssetScenario: ScenarioBuilder = scenario("BlockchainSendAsset")
//    .feed(FromFeeder.fromFeed)
//    .feed(PasswordFeeder.passwordFeed)
//    .feed(ToFeeder.toFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec(http("BlockchainSendAsset_GET")
//      .get(routes.SendAssetController.blockchainSendAssetForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("BlockchainSendAsset_POST")
//      .post(routes.SendAssetController.blockchainSendAsset().url)
//      .formParamMap(Map(
//        Form.FROM -> "${%s}".format(Test.TEST_FROM),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
//        Form.TO -> "${%s}".format(Test.TEST_TO),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//}