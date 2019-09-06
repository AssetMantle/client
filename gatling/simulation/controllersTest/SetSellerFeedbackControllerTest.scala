//package controllersTest
//
//import constants.{Form, Test}
//import controllers.routes
//import feeders._
//import io.gatling.core.Predef._
//import io.gatling.core.structure.ScenarioBuilder
//import io.gatling.http.Predef._
//
//class SetSellerFeedbackControllerTest extends Simulation {
//
//  val scenarioBuilder: ScenarioBuilder = setSellerFeedbackControllerTest.setSellerFeedbackScenario
//  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
//}
//
//object setSellerFeedbackControllerTest {
//
//  val setSellerFeedbackScenario: ScenarioBuilder = scenario("SetSellerFeedback")
//    .feed(PasswordFeeder.passwordFeed)
//    .feed(ToFeeder.toFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(RatingFeeder.ratingFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec(http("SetSellerFeedback_GET")
//      .get(routes.SetSellerFeedbackController.setSellerFeedbackForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("SetSellerFeedback_POST")
//      .post(routes.SetSellerFeedbackController.setSellerFeedback().url)
//      .formParamMap(Map(
//        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
//        Form.TO -> "${%s}".format(Test.TEST_TO),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.RATING -> "${%s}".format(Test.TEST_RATING),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//
//  val blockchainSetSellerFeedbackScenario: ScenarioBuilder = scenario("BlockchainSetSellerFeedback")
//    .feed(FromFeeder.fromFeed)
//    .feed(PasswordFeeder.passwordFeed)
//    .feed(ToFeeder.toFeed)
//    .feed(PegHashFeeder.pegHashFeed)
//    .feed(RatingFeeder.ratingFeed)
//    .feed(GasFeeder.gasFeed)
//    .exec(http("BlockchainSetSellerFeedback_GET")
//      .get(routes.SetSellerFeedbackController.blockchainSetSellerFeedbackForm().url)
//      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
//    .pause(2)
//    .exec(http("BlockchainSetSellerFeedback_POST")
//      .post(routes.SetSellerFeedbackController.blockchainSetSellerFeedback().url)
//      .formParamMap(Map(
//        Form.FROM -> "${%s}".format(Test.TEST_FROM),
//        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
//        Form.TO -> "${%s}".format(Test.TEST_TO),
//        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
//        Form.RATING -> "${%s}".format(Test.TEST_RATING),
//        Form.GAS -> "${%s}".format(Test.TEST_GAS),
//        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
//}