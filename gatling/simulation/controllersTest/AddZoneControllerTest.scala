package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class AddZoneControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = addZoneControllerTest.addZoneScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object addZoneControllerTest {

  val addZoneScenario: ScenarioBuilder = scenario("AddZone")
    .feed(NameFeeder.nameFeed)
    .feed(CurrencyFeeder.currencyFeed)
    .exec(http("AddZone_GET")
      .get(routes.AddZoneController.addZoneForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("AddZone_POST")
      .post(routes.AddZoneController.addZoneForm().url)
      .formParamMap(Map(
        Form.NAME -> "${%s}".format(Test.TEST_NAME),
        Form.CURRENCY -> "${%s}".format(Test.TEST_CURRENCY),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val verifyZoneScenario: ScenarioBuilder = scenario("VerifyZone")
    .feed(ZoneIDFeeder.zoneIDFeed)
    .feed(PasswordFeeder.passwordFeed)
  /*
      .exec(http("VerifyZone_GET")
        .get(routes.AddZoneController.verifyZoneForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
      .pause(2)
      .exec(http("VerifyZone_POST")
        .post(routes.AddZoneController.verifyZone().url)
    .formParamMap(Map(
      Form.ZONE_ID ->  "${%s}".format(Test.TEST_ZONE_ID),
      Form.PASSWORD ->  "${%s}".format(Test.TEST_PASSWORD),
      Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
     .pause(5)
  */

  val rejectVerifyZoneScenario: ScenarioBuilder = scenario("RejectVerifyZone")
    .feed(ZoneIDFeeder.zoneIDFeed)
  /*
  .exec(http("RejectVerifyZone_GET")
    .get(routes.AddZoneController.rejectVerifyZoneRequestForm().url)
    .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
  .pause(2)
  .exec(http("RejectVerifyZone_POST")
    .post(routes.AddZoneController.rejectVerifyZoneRequest().url)
    .formParamMap(Map(
      Form.ZONE_ID ->  "${%s}".format(Test.TEST_ZONE_ID),
      Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
*/

  val blockchainAddZoneScenario: ScenarioBuilder = scenario("BlockchainAddZone")
    .feed(FromFeeder.fromFeed)
    .feed(ToFeeder.toFeed)
    .feed(ZoneIDFeeder.zoneIDFeed)
    .feed(PasswordFeeder.passwordFeed)
    .exec(http("BlockchainAddZone_GET")
      .get(routes.AddZoneController.blockchainAddZoneForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainAddZone_POST")
      .post(routes.AddZoneController.blockchainAddZone().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)


}