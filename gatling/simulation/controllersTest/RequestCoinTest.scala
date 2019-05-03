package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders.{PasswordFeeder, UsernameFeeder}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class RequestCoinTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = scenario("RequestCoin")
    .feed(UsernameFeeder.apply())
    .feed(PasswordFeeder.apply())
    .exec(controllersTest.loginTest.loginAfterSignUpScenario)
    .exec(http("RequestCoin_GET")
      .get(routes.SendCoinController.requestCoinsForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("RequestCoin_POST")
      .post(routes.SendCoinController.requestCoins().url)
      .formParamMap(Map(Form.COUPON -> "", Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  setUp(scenarioBuilder.inject(atOnceUsers(3))).protocols(http.baseUrl(Test.BASE_URL))
}