/*
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
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .exec(http("Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Test.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
        Test.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Test.PUSH_NOTIFICATION_TOKEN -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("${%s}".format(Test.TEST_USERNAME)).exists)
    )
    .pause(3)


  val loginScenario: ScenarioBuilder = scenario("Login Before SignUp")
    .exec(http("Login_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format("Login")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Test.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
        Test.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Test.PUSH_NOTIFICATION_TOKEN -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("${%s}".format(Test.TEST_USERNAME)).exists)
    )
    .pause(5)

  val loginMain: ScenarioBuilder = scenario("LoginMain")
    .feed(GenesisFeeder.genesisFeed)
    .exec(http("Login_Main_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("legend:contains(%s)".format("Login")).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .exec(http("Login_Main_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Test.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Test.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Test.PUSH_NOTIFICATION_TOKEN -> "",
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("${%s}".format(Test.TEST_MAIN_USERNAME)).exists)
    )
    .pause(5)
}
*/
