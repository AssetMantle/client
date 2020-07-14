package controllersTest

import constants.{Form, Test}
import controllers.routes

import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object addKeyControllerTest {

  val addKeyScenario: ScenarioBuilder = scenario("AddKey")
    .feed(NameFeeder.nameFeed)
    .feed(PasswordFeeder.passwordFeed)
    .exec(http("AddKey_GET")
      .get(routes.AddKeyController.blockchainAddKeyForm().url)
      .check(status.is(200))
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_ADD_KEY.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(Test.REQUEST_DELAY)
    .exec(http("AddKey_POST")
      .post(routes.AddKeyController.blockchainAddKey().url)
      .formParamMap(Map(
        constants.FormField.NAME.name -> "${%s}".format(Test.TEST_NAME),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(200))
      .check(substring("SUCCESS KEY_ADDED").exists)
    )

}