package controllersTest

import constants.{Form, Test}
import controllers.routes

import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class MasterTest9 extends Simulation {

  val scenarioBuilderTraders: ScenarioBuilder =
    scenario("signUp/dbOPs")
        .exec(masterTest8.dbCalls)

  setUp(
    scenarioBuilderTraders.inject(atOnceUsers(10))
  ).maxDuration(800)
    .protocols(http.baseUrl(Test.BASE_URL))

}

object masterTest8 {

  val masterTestSignUp: ScenarioBuilder = scenario("masterTestSignUp")
    .feed(UsernameFeeder.usernameFeed)
    .feed(PasswordFeeder.passwordFeed)
    .exec(http("SignUp_GET")
      .get(routes.AccountController.signUpForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("SignUp_POST")
      .post(routes.AccountController.signUp().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_USERNAME),
        Form.USERNAME_AVAILABLE -> true,
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CONFIRM_PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(8)

  val dbCalls: ScenarioBuilder = scenario("dbOps")
    .feed(UsernameFeeder.usernameFeed)
    .exec(http("testGet")
        .get(routes.IndexController.testAction("${%s}".format(Test.TEST_USERNAME)).url))
    .pause(2)

}