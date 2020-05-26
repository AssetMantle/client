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
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(2)
    .exec(http("SignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        Test.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
        Test.USERNAME_AVAILABLE -> true,
        Test.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CONFIRM_PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Create Wallet").exists)
    )
    .pause(2)
    .exec(http("CreateWallet_GET")
      .get(session=>routes.AccountController.createWalletForm(session(Test.TEST_USERNAME).as[String]).url)
      .check(substring("Create Wallet").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      .check(css("[name=%s]".format(Test.MNEMONICS), "value").saveAs(Test.MNEMONICS))
    )
    .pause(2)
    .exec(http("CreateWallet_POST")
      .post(routes.AccountController.createWallet().url)
      .formParamMap(Map(
        Test.USERNAME -> ("${%s}".format(Test.TEST_USERNAME)),
        Test.MNEMONICS->"${%s}".format(Test.MNEMONICS),
        Test.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("Signed Up").exists)
    )
    .pause(5)
}

*/
