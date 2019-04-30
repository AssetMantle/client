package gatlingTest

import constants.{Form, Test}
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.util.Random

class RequestCoinTest extends Simulation {

  val httpProtocol: HttpProtocolBuilder = http.baseUrl(Test.BASE_URL)

  val arrayUsernameFeeder = Array(
    Map(Test.TEST_USERNAME  -> "00000001"),
    Map(Test.TEST_USERNAME  -> "00000002"),
    Map(Test.TEST_USERNAME  -> "00000003")
  )

  val usernameFeeder: Iterator[Map[String, String]] = Iterator.continually(Map(Test.TEST_USERNAME -> Random.alphanumeric.take(8).mkString))

  val scenarioBuilder: ScenarioBuilder = scenario("RequestCoin Scenario")
    .feed(arrayUsernameFeeder)
    .exec(http("Login_GET")
      .get(routes.LoginController.loginForm().url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))

    .exec(http("Login_POST")
      .post(routes.LoginController.login().url)
      .formParamMap(Map(Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME), Form.PASSWORD -> "123456789", Form.NOTIFICATION_TOKEN -> "", Test.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

    .exec(http("RequestCoin_GET")
      .get(routes.SendCoinController.requestCoinsForm().url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))

    .exec(http("RequestCoin_POST")
      .post(routes.SendCoinController.requestCoins().url)
      .formParamMap(Map(Form.COUPON -> "", Test.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

    .pause(5)

  setUp(scenarioBuilder.inject(atOnceUsers(3)))
    .protocols(httpProtocol)
}