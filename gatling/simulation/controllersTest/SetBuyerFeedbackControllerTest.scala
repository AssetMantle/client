package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SetBuyerFeedbackControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = setBuyerFeedbackControllerTest.setBuyerFeedbackScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object setBuyerFeedbackControllerTest {

  val setBuyerFeedbackScenario: ScenarioBuilder = scenario("SetBuyerFeedback")
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(RatingFeeder.ratingFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("SetBuyerFeedback_GET")
      .get(routes.SetBuyerFeedbackController.setBuyerFeedbackForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("SetBuyerFeedback_POST")
      .post(routes.SetBuyerFeedbackController.setBuyerFeedback().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.RATING -> "${%s}".format(Test.TEST_RATING),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val blockchainSetBuyerFeedbackScenario: ScenarioBuilder = scenario("BlockchainSetBuyerFeedback")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(RatingFeeder.ratingFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainSetBuyerFeedback_GET")
      .get(routes.SetBuyerFeedbackController.blockchainSetBuyerFeedbackForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainSetBuyerFeedback_POST")
      .post(routes.SetBuyerFeedbackController.blockchainSetBuyerFeedback().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.RATING -> "${%s}".format(Test.TEST_RATING),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}