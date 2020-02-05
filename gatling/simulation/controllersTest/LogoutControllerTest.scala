package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class LogoutControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = logoutControllerTest.logoutScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object logoutControllerTest {

  val logoutScenario: ScenarioBuilder = scenario("Logout")
    .exec(http("Logout_Form_GET")
      .get(routes.AccountController.logoutForm().url)
      .check(css("legend:contains(%s)".format("Logout")).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Logout_POST")
      .post(routes.AccountController.logout().url)
      .formParamMap(Map(
        Form.RECEIVE_NOTIFICATIONS -> false,
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS LOGGED_OUT").exists)
    )
    .pause(2)
}
