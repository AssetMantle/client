package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class RedeemFiatControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = redeemFiatControllerTest.redeemFiatScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object redeemFiatControllerTest {

  val redeemFiatScenario: ScenarioBuilder = scenario("RedeemFiat")
    .feed(RedeemAmountFeeder.redeemAmountFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("Redeem_Fiat_GET")
      .get(session=> routes.RedeemFiatController.redeemFiatForm(session(Test.TEST_SELLER_USERNAME).as[String]).url)
      .check(css("legend:contains(%s)".format("Redeem Fiat")).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Redeem_Fiat_POST")
      .post(routes.RedeemFiatController.redeemFiat().url)
      .formParamMap(Map(
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.REDEEM_AMOUNT -> "${%s}".format(Test.TEST_REDEEM_AMOUNT),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_SELLER_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS FIAT_REDEEMED").exists)
    )
    .pause(2)

  val blockchainRedeemFiatScenario: ScenarioBuilder = scenario("BlockchainRedeemFiat")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(RedeemAmountFeeder.redeemAmountFeed)
    .feed(GasFeeder.gasFeed)
    .feed(ModeFeeder.modeFeed)
    .exec(http("BlockchainRedeemFiat_GET")
      .get(routes.RedeemFiatController.blockchainRedeemFiatForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_REDEEM_FIAT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainRedeemFiat_POST")
      .post(routes.RedeemFiatController.blockchainRedeemFiat().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.REDEEM_AMOUNT -> "${%s}".format(Test.TEST_REDEEM_AMOUNT),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS FIAT_REDEEMED").exists)
    )
}
