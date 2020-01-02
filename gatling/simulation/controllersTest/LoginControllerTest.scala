package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders.{GenesisFeeder, PasswordFeeder, UsernameFeeder}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class LoginControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = loginControllerTest.loginMain
  setUp(scenarioBuilder.inject(atOnceUsers(2))).protocols(http.baseUrl(Test.BASE_URL))

}

object loginControllerTest {

  val loginAfterSignUpScenario: ScenarioBuilder = scenario("Login After SignUp")
    .feed(UsernameFeeder.usernameFeed)
    .feed(PasswordFeeder.passwordFeed)
    .exec(controllersTest.signUpControllerTest.signUpScenario)
    .exec(http("Login_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("${%s}".format(Test.TEST_USERNAME)).exists)
    )
    .pause(3)


  val loginScenario: ScenarioBuilder = scenario("Login Before SignUp")
    .exec(http("Login_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("${%s}".format(Test.TEST_USERNAME)).exists)
    )
    .pause(5)

  val loginMain: ScenarioBuilder = scenario("LoginMain")
    .feed(GenesisFeeder.genesisFeed)
    .exec(http("Login_Main_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.LOGIN.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("Login_Main_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("${%s}".format(Test.TEST_MAIN_USERNAME)).exists)
    )
    .pause(5)
}
