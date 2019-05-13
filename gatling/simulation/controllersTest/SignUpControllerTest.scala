package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SignUpControllerTest extends Simulation {
  val scenarioBuilder: ScenarioBuilder = signUpControllerTest.signUpScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object signUpControllerTest {

  val signUpScenario: ScenarioBuilder = scenario("SignUp")
    .feed(UsernameFeeder.usernameFeed)
    .feed(PasswordFeeder.passwordFeed)

    .exec { session => {
      println("*******************************************************************************************************-")
      println(session(Test.TEST_USERNAME).as[String]+"||")
      println(session(Test.TEST_PASSWORD).as[String]+"||")
      println("*******************************************************************************************************-")
      session
    }}
    .exec(http("SignUp_GET")
      .get(routes.SignUpController.signUpForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("SignUp_POST")
      .post(routes.SignUpController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}