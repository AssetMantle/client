package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SendFiatControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = sendFiatControllerTest.sendFiatScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object sendFiatControllerTest {

  val sendFiatScenario: ScenarioBuilder = scenario("SendFiat")
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(AmountFeeder.amountFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("SendFiat_GET")
      .get(routes.SendFiatController.sendFiatForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("SendFiat_POST")
      .post(routes.SendFiatController.sendFiat().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.AMOUNT -> "${%s}".format(Test.TEST_AMOUNT),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val blockchainSendFiatScenario: ScenarioBuilder = scenario("BlockchainSendFiat")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(AmountFeeder.amountFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainSendFiat_GET")
      .get(routes.SendFiatController.blockchainSendFiatForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainSendFiat_POST")
      .post(routes.SendFiatController.blockchainSendFiat().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.AMOUNT -> "${%s}".format(Test.TEST_AMOUNT),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}