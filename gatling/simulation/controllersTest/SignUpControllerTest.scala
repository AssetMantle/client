package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SignUpControllerTest extends Simulation {
  val scenarioBuilder: ScenarioBuilder = signUpControllerTest.signUpScenario
  setUp(scenarioBuilder.inject(atOnceUsers(400))).protocols(http.baseUrl(Test.BASE_URL))
}

object signUpControllerTest {

  val signUpScenario: ScenarioBuilder = scenario("SignUp")
    .exec(http("SignUp_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.SIGN_UP.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("SignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
        Form.USERNAME_AVAILABLE -> true,
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CONFIRM_PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(css("div:contains(%s)".format(constants.FormField.CONFIRM_NOTE_NEW_KEY_DETAILS.name)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN))
      .check(regex("""blockchainAddress=([^&]*)""").saveAs(Test.TEST_BLOCKCHAIN_ADDRESS))
      .check(regex("""publicKey=([^&]*)""").saveAs(Test.TEST_PUBLIC_KEY))
      .check(regex("""seed=([^"]*)""").saveAs(Test.TEST_SEED))
    )
    .pause(1)
    .exec(http("Note_New_Key_Details")
      .post(session=> routes.AccountController.noteNewKeyDetails(session(Test.TEST_USERNAME).as[String],session(Test.TEST_BLOCKCHAIN_ADDRESS).as[String],session(Test.TEST_PUBLIC_KEY).as[String],session(Test.TEST_SEED).as[String].replace('+',' ')).url)
      .formParamMap(Map(
        Form.CONFIRM_NOTE_NEW_KEY_DETAILS-> true,
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS SIGNED_UP").exists)
    )
    .pause(5)
}