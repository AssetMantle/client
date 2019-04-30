package gatlingTest

import constants.{Form, Test}
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.util.Random

class SignUpTest extends Simulation {

  val httpProtocol: HttpProtocolBuilder = http.baseUrl(Test.BASE_URL)

  val arrayUsernameFeeder = Array(
    Map(Test.TEST_USERNAME  -> "00000001"),
    Map(Test.TEST_USERNAME  -> "00000002"),
    Map(Test.TEST_USERNAME  -> "00000003")
  )

  val usernameFeeder: Iterator[Map[String, String]] = Iterator.continually(Map(Test.TEST_USERNAME -> Random.alphanumeric.take(10).mkString))

  val scenarioBuilder: ScenarioBuilder = scenario("SignUp Scenario")
    .feed(arrayUsernameFeeder)

    .exec(http("SignUp_GET")
      .get(routes.SignUpController.signUpForm().url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))

    .exec(http("SignUp_POST")
      .post(routes.SignUpController.signUp().url)
      .formParamMap(Map(Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME), Form.PASSWORD -> "123456789", Test.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

    .pause(5)

  setUp(scenarioBuilder.inject(atOnceUsers(3)))
    .protocols(httpProtocol)
}