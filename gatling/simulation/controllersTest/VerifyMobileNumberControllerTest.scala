package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders.OTPFeeder
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class VerifyMobileNumberControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = verifyMobileNumberControllerTest.verifyMobileNumberScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object verifyMobileNumberControllerTest {

  val verifyMobileNumberScenario: ScenarioBuilder = scenario("VerifyMobileNumber")
    .feed(OTPFeeder.otpFeed)
    .exec(http("VerifyMobileNumber_GET")
      .get(routes.VerifyMobileNumberController.verifyMobileNumberForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("VerifyMobileNumber_POST")
      .post(routes.VerifyMobileNumberController.verifyMobileNumber().url)
      .formParamMap(Map(
        Form.OTP -> "${%s}".format(Test.TEST_OTP),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
}