package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders.{GenesisFeeder, PasswordFeeder, UsernameFeeder}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class LoginControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = loginControllerTest.loginAfterSignUpScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))

}
  object loginControllerTest {

    val loginAfterSignUpScenario: ScenarioBuilder = scenario("Login After SignUp")

      .exec(controllersTest.signUpControllerTest.signUpScenario)

      .exec(http("Login_GET")
        .get(routes.LoginController.loginForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
      .exec(http("Login_POST")
        .post(routes.LoginController.login().url)
        .formParamMap(Map(
          Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
          Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
          Form.NOTIFICATION_TOKEN -> "",
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
      .pause(5)


    val loginWithoutSignUpScenario: ScenarioBuilder = scenario("Login Before SignUp")
      .exec { session => {
        println("*******************************************************************************************************-")
        println(session(Test.TEST_USERNAME).as[String] + "||")
        println(session(Test.TEST_PASSWORD).as[String] + "||")
        println("*******************************************************************************************************-")
        session
      }
      }
      .exec(http("Login_GET")
        .get(routes.LoginController.loginForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
      .exec(http("Login_POST")
        .post(routes.LoginController.login().url)
        .formParamMap(Map(
          Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
          Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
          Form.NOTIFICATION_TOKEN -> "",
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
      .pause(5)

    val loginMain: ScenarioBuilder = scenario("LoginMain")
      .feed(GenesisFeeder.genesisFeed)
      .exec { session => {
        println("*******************************************************************************************************-")
        println(session(Test.TEST_USERNAME).as[String] + "||")
        println(session(Test.TEST_PASSWORD).as[String] + "||")
        println("*******************************************************************************************************-")
        session
      }
      }
      .exec(http("Login_GET")
        .get(routes.LoginController.loginForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
      .exec(http("Login_POST")
        .post(routes.LoginController.login().url)
        .formParamMap(Map(
          Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
          Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
          Form.NOTIFICATION_TOKEN -> "",
          Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
      .pause(5)
  }
