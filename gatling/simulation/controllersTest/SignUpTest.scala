package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders.{PasswordFeeder, UsernameFeeder}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SignUpTest extends Simulation {
  val scenarioBuilder: ScenarioBuilder = signUpTest.signUpScenario
  setUp(scenarioBuilder.inject(atOnceUsers(3))).protocols(http.baseUrl(Test.BASE_URL))
}

object signUpTest {

  val signUpScenario: ScenarioBuilder = scenario("SignUp")
    .feed(UsernameFeeder.apply())
    .feed(PasswordFeeder.apply())
    .exec(http("SignUp_GET")
      .get(routes.SignUpController.signUpForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("SignUp_POST")
      .post(routes.SignUpController.signUp().url)
      .formParamMap(Map(Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME), Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD), Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}