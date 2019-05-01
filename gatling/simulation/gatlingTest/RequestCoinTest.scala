package gatlingTest

import constants.{Form, Test}
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

class RequestCoinTest extends Simulation {

  val httpProtocol: HttpProtocolBuilder = http.baseUrl(Test.BASE_URL)

  val users: Int = 3

  val scenarioBuilder: ScenarioBuilder = scenario("RequestCoin Scenario")
    .feed(feeders.usernameFeeder.customFeeder(users))
    .feed(feeders.passwordFeeder.customFeeder(users))

    .exec(http("Login_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))

    .exec(http("Login_POST")
      .post(routes.LoginController.login().url)
      .formParamMap(Map(Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME), Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD), Form.NOTIFICATION_TOKEN -> "", Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

    .exec(http("RequestCoin_GET")
      .get(routes.SendCoinController.requestCoinsForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))

    .exec(http("RequestCoin_POST")
      .post(routes.SendCoinController.requestCoins().url)
      .formParamMap(Map(Form.COUPON -> "", Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

    .pause(5)

  setUp(scenarioBuilder.inject(atOnceUsers(users)))
    .protocols(httpProtocol)
}