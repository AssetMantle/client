//package controllersTest
//
//import constants.{Form, Test}
//import controllers.routes
//import feeders._
//import io.gatling.core.Predef._
//import io.gatling.core.structure.ScenarioBuilder
//import io.gatling.http.Predef._
//
//class ReleaseAssetControllerTest extends Simulation {
//
//  val scenarioBuilder: ScenarioBuilder = releaseAssetControllerTest.releaseAssetScenario
//  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
//}
//
//object releaseAssetControllerTest {
//
//  val releaseAssetScenario: ScenarioBuilder = scenario("ReleaseAsset")
//    .feed(ToFeeder.toFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(PasswordFeeder.passwordFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec(http("ReleaseAsset_GET")
//      .get(routes.ReleaseAssetController.releaseAssetForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("ReleaseAsset_POST")
//      .post(routes.ReleaseAssetController.releaseAsset().url)
//      .formParamMap(Map(
//        Form.TO -> "${%s}".format(Test.TEST_TO),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//
//  val blockchainReleaseAssetScenario: ScenarioBuilder = scenario("blockchainReleaseAsset")
//    .feed(FromFeeder.fromFeed)
//    .feed(ToFeeder.toFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(PasswordFeeder.passwordFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec(http("BlockchainReleaseAsset_GET")
//      .get(routes.ReleaseAssetController.blockchainReleaseAssetForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("BlockchainReleaseAsset_POST")
//      .post(routes.ReleaseAssetController.blockchainReleaseAsset().url)
//      .formParamMap(Map(
//        Form.FROM -> "${%s}".format(Test.TEST_FROM),
//        Form.TO -> "${%s}".format(Test.TEST_TO),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//}