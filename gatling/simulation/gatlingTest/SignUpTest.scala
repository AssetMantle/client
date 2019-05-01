package gatlingTest

import constants.{Form, Test}
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

class SignUpTest extends Simulation {

  val httpProtocol: HttpProtocolBuilder = http.baseUrl(Test.BASE_URL)

  val users: Int = 3

  val scenarioBuilder: ScenarioBuilder = scenario("SignUp Scenario")
    .feed(feeders.usernameFeeder.customFeeder(users))
    .feed(feeders.passwordFeeder.customFeeder(users))

    .exec(http("SignUp_GET")
      .get(routes.SignUpController.signUpForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))

    .exec(http("SignUp_POST")
      .post(routes.SignUpController.signUp().url)
      .formParamMap(Map(Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME), Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD), Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

    .pause(5)

  setUp(scenarioBuilder.inject(atOnceUsers(users)))
    .protocols(httpProtocol)
}