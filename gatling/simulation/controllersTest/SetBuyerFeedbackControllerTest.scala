/*
package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SetBuyerFeedbackControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = setBuyerFeedbackControllerTest.setBuyerFeedbackScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object setBuyerFeedbackControllerTest {

  val setBuyerFeedbackScenario: ScenarioBuilder = scenario("SetBuyerFeedback")
    .feed(RatingFeeder.ratingFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("Set_Buyer_Feedback_GET")
      .get(session=> routes.SetBuyerFeedbackController.setBuyerFeedbackForm(session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.SET_BUYER_FEEDBACK.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("SetBuyerFeedback_POST")
      .post(routes.SetBuyerFeedbackController.setBuyerFeedback().url)
      .formParamMap(Map(
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.RATING -> "${%s}".format(Test.TEST_RATING),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Test.PASSWORD ->"${%s}".format(Test.TEST_BUYER_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("SUCCESS BUYER_FEEDBACK_SET").exists)
    )
    .pause(3)

  val blockchainSetBuyerFeedbackScenario: ScenarioBuilder = scenario("BlockchainSetBuyerFeedback")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(RatingFeeder.ratingFeed)
    .feed(GasFeeder.gasFeed)
    .feed(ModeFeeder.modeFeed)
    .exec(http("BlockchainSetBuyerFeedback_GET")
      .get(routes.SetBuyerFeedbackController.blockchainSetBuyerFeedbackForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_SET_BUYER_FEEDBACK.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainSetBuyerFeedback_POST")
      .post(routes.SetBuyerFeedbackController.blockchainSetBuyerFeedback().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.RATING -> "${%s}".format(Test.TEST_RATING),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Test.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("SUCCESS BUYER_FEEDBACK_SET").exists)
    )
}
*/
