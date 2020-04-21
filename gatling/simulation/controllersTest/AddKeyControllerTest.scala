package controllersTest

import constants.{Form, Test}
import controllers.routes

import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class AddKeyControllerTest extends Simulation {
  val scenarioBuilder: ScenarioBuilder = addKeyControllerTest.addKeyScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object addKeyControllerTest {

  val addKeyScenario: ScenarioBuilder = scenario("AddKey")
    .feed(NameFeeder.nameFeed)
    .feed(PasswordFeeder.passwordFeed)
    .exec(http("AddKey_GET")
      .get(routes.AddKeyController.blockchainAddKeyForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_ADD_KEY.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("AddKey_POST")
      .post(routes.AddKeyController.blockchainAddKey().url)
      .formParamMap(Map(
        Form.NAME -> "${%s}".format(Test.TEST_NAME),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS KEY_ADDED").exists)
    )

}