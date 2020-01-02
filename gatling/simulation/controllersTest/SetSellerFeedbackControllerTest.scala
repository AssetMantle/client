package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SetSellerFeedbackControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = setSellerFeedbackControllerTest.setSellerFeedbackScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object setSellerFeedbackControllerTest {

  val setSellerFeedbackScenario: ScenarioBuilder = scenario("SetSellerFeedback")

    .feed(RatingFeeder.ratingFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("Set_Seller_Feedback_Form_GET")
      .get(session=> routes.SetSellerFeedbackController.setSellerFeedbackForm(session(Test.TEST_BUYER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.SET_SELLER_FEEDBACK.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Set_Seller_Feedback_POST")
      .post(routes.SetSellerFeedbackController.setSellerFeedback().url)
      .formParamMap(Map(
        Form.BUYER_ADDRESS -> "${%s}".format(Test.TEST_BUYER_ADDRESS),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.RATING -> "${%s}".format(Test.TEST_RATING),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS SELLER_FEEDBACK_SET").exists)
    )
    .pause(3)

  val blockchainSetSellerFeedbackScenario: ScenarioBuilder = scenario("BlockchainSetSellerFeedback")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(RatingFeeder.ratingFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainSetSellerFeedback_GET")
      .get(routes.SetSellerFeedbackController.blockchainSetSellerFeedbackForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_SET_SELLER_FEEDBACK.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainSetSellerFeedback_POST")
      .post(routes.SetSellerFeedbackController.blockchainSetSellerFeedback().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.RATING -> "${%s}".format(Test.TEST_RATING),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS SELLER_FEEDBACK_SET").exists)
    )
}
