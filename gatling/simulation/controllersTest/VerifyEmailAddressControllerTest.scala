package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders.OTPFeeder
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class VerifyEmailAddressControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = verifyEmailAddressControllerTest.verifyEmailAddressScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object verifyEmailAddressControllerTest {

  val verifyEmailAddressScenario: ScenarioBuilder = scenario("VerifyEmailAddress")
    .feed(OTPFeeder.otpFeed)
    .exec(http("VerifyEmailAddress_GET")
      .get(routes.VerifyEmailAddressController.verifyEmailAddressForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("VerifyEmailAddress_POST")
      .post(routes.VerifyEmailAddressController.verifyEmailAddress().url)
      .formParamMap(Map(
        Form.OTP -> "${%s}".format(Test.TEST_OTP),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
}
