/*
package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class BlockchainTest extends Simulation {

  setUp(
    blockchainTest.blockChainTestSignUp4.inject(atOnceUsers(4)),
    blockchainTest.loginMain.inject(atOnceUsers(1)),
    blockchainTest.blockChainTestSendCoinFromMainTo4.inject(atOnceUsers(1), nothingFor(14), atOnceUsers(1), nothingFor(14), atOnceUsers(1), nothingFor(14), atOnceUsers(1), nothingFor(14))
  ).protocols(http.baseUrl(Test.BASE_URL))
}

object blockchainTest {

  val blockChainTestSignUp4: ScenarioBuilder = scenario("blockChainTestSignUp4")
    .exec(controllersTest.signUpControllerTest.signUpScenario)

  val loginMain: ScenarioBuilder = scenario("loginMain")
    .pause(10)
    .feed(GenesisFeeder.genesisFeed)
    .exec(http("Login_GET")
      .get(routes.AccountController.loginForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .exec(http("Login_POST")
      .post(routes.AccountController.login().url)
      .formParamMap(Map(
        Form.USERNAME -> "${%s}".format(Test.TEST_MAIN_USERNAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.PUSH_NOTIFICATION_TOKEN -> "",
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val blockChainTestSendCoinFromMainTo4: ScenarioBuilder = scenario("blockChainTestSendCoinFromMainTo4")
    .pause(20)
    .feed(GenesisFeeder.genesisFeed)
    .feed(UsernameFeeder.usernameFeed)
    .exec(controllersTest.sendCoinControllerTest.blockchainSendCoinMainScenario)
}
*/
