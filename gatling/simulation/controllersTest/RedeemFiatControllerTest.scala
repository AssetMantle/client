package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class RedeemFiatControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = redeemFiatControllerTest.redeemFiatScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object redeemFiatControllerTest {

  val redeemFiatScenario: ScenarioBuilder = scenario("RedeemFiat")
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(RedeemAmountFeeder.redeemAmountFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("RedeemFiat_GET")
      .get(routes.RedeemFiatController.redeemFiatForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RedeemFiat_POST")
      .post(routes.RedeemFiatController.redeemFiat().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.REDEEM_AMOUNT -> "${%s}".format(Test.TEST_REDEEM_AMOUNT),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val blockchainRedeemFiatScenario: ScenarioBuilder = scenario("BlockchainRedeemFiat")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(RedeemAmountFeeder.redeemAmountFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainRedeemFiat_GET")
      .get(routes.RedeemFiatController.blockchainRedeemFiatForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainRedeemFiat_POST")
      .post(routes.RedeemFiatController.blockchainRedeemFiat().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.REDEEM_AMOUNT -> "${%s}".format(Test.TEST_REDEEM_AMOUNT),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
}