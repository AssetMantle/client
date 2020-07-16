package scripts

import constants.Test
import controllers.routes
import controllersTest._
import feeders.JDBCFeeder._
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SignUpConstraint extends Simulation {

  setUp(
    passwordMismatch.mismatchPasswordScenario.inject(atOnceUsers(1))
  ).protocols(http.baseUrl(Test.BASE_URL))
}

object passwordMismatch{

  val mismatchPasswordScenario: ScenarioBuilder = scenario("SignUp")
    .exec(http("SignUp_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Register)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("SignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        constants.FormField.USERNAME.name -> "${%s}".format(Test.TEST_USERNAME),
        constants.FormField.USERNAME_AVAILABLE.name -> true,
        constants.FormField.SIGNUP_PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        constants.FormField.SIGNUP_CONFIRM_PASSWORD.name -> ("${%s}".format(Test.TEST_PASSWORD)+"XYZ"),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(400))
      .check(css("legend:contains(Passwords Do Not Match)").exists)
    )
    .pause(Test.REQUEST_DELAY)

}

object UsernameUnavailable{
  val usernameUnavailable: ScenarioBuilder = scenario("UsernameUnavailable")
    .exec(http("SignUp_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(status.is(200))
      .check(css("legend:contains(Register)").exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
    )
    .pause(Test.REQUEST_DELAY)
    .exec(http("SignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        constants.FormField.USERNAME.name -> "${%s}".format(Test.TEST_USERNAME),
        constants.FormField.USERNAME_AVAILABLE.name -> true,
        constants.FormField.SIGNUP_PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        constants.FormField.SIGNUP_CONFIRM_PASSWORD.name -> ("${%s}".format(Test.TEST_PASSWORD)+"XYZ"),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(400))
      .check(css("legend:contains(UserName UnAvailable)").exists)
    )
    .pause(Test.REQUEST_DELAY)


}
