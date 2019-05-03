package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders.{PasswordFeeder, UsernameFeeder}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class LoginTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = loginTest.loginAfterSignUpScenario
  setUp(scenarioBuilder.inject(atOnceUsers(3))).protocols(http.baseUrl(Test.BASE_URL))
}

object loginTest {

  val loginAfterSignUpScenario: ScenarioBuilder = scenario("Login Scenario")
    .feed(UsernameFeeder.apply())
    .feed(PasswordFeeder.apply())
    .exec(controllersTest.signUpTest.signUpScenario)
    .exec(http("Login_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("Login_POST")
      .post(routes.LoginController.login().url)
      .formParamMap(Map(Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME), Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD), Form.NOTIFICATION_TOKEN -> "", Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)


  val loginWithoutSignUpScenario: ScenarioBuilder = scenario("Login Scenario")
    .feed(UsernameFeeder.apply())
    .feed(PasswordFeeder.apply())
    .exec(http("Login_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("Login_POST")
      .post(routes.LoginController.login().url)
      .formParamMap(Map(Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME), Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD), Form.NOTIFICATION_TOKEN -> "", Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

}