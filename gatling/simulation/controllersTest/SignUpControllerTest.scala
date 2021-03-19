/*
package controllersTest

import constants.{Form, Test}
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SignUpControllerTest extends Simulation {
  val scenarioBuilder: ScenarioBuilder =  signUpControllerTest.signUpScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object signUpControllerTest {

  val signUpScenario: ScenarioBuilder = scenario("SignUp")
    .exec(http("SignUp_GET")
        .get(routes.AccountController.signUpForm().url)
      .check(substring("Register").exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
    )
    .exec{session=>println(session)
    session}
    .pause(2)
    .exec(http("SignUp_GET")
      .get(session=>routes.AccountController.signUpForm(session(Test.TEST_BLOCKCHAIN_ADDRESS).as[String].replaceAll("[,(']","")).url)
      .check(css("legend:contains(%s)".format("Sign Up")).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.MNEMONIC), "value").saveAs(Form.MNEMONIC))
    )
    .pause(2)
    .exec(http("SignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> ("${%s}".format(Test.TEST_USERNAME)),
        Form.USERNAME_AVAILABLE -> true,
        Form.MNEMONIC->"${%s}".format(Form.MNEMONIC),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CONFIRM_PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("Signed Up").exists)
    )
    .pause(5)
}*/
