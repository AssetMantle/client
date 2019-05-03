package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders.{PasswordFeeder, UsernameFeeder}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

class SignUpTest extends Simulation {

  val httpProtocol: HttpProtocolBuilder = http.baseUrl(Test.BASE_URL)

  val users: Int = 3

  val scenarioBuilder: ScenarioBuilder = scenario("SignUp Scenario")

    .feed(UsernameFeeder.apply())
    .feed(PasswordFeeder.apply())

    .exec { session =>
      println(session(Test.TEST_USERNAME).as[String], session(Test.ID).as[String])
      println(session(Test.TEST_PASSWORD).as[String], session(Test.ID).as[String])
      session
    }

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