package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class SendFiatControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = sendFiatControllerTest.sendFiatScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object sendFiatControllerTest {

  val sendFiatScenario: ScenarioBuilder = scenario("SendFiat")
    .feed(AmountFeeder.amountFeed)
    .exec(http("Send_Fiat_Form_GET")
      .get(session=>routes.SendFiatController.sendFiatForm(session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String],session(Test.TEST_AMOUNT).as[Int]).url)
      .check(css("legend:contains(%s)".format(constants.Form.SEND_FIAT.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Send_Fiat_POST")
      .post(routes.SendFiatController.sendFiat().url)
      .formParamMap(Map(
        Test.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.AMOUNT -> "${%s}".format(Test.TEST_AMOUNT),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)
      ))
      .check(substring("SUCCESS FIAT_SENT").exists)
    )

  val blockchainSendFiatScenario: ScenarioBuilder = scenario("BlockchainSendFiat")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(AmountFeeder.amountFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .feed(ModeFeeder.modeFeed)
    .exec(http("BlockchainSendFiat_GET")
      .get(routes.SendFiatController.blockchainSendFiatForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_SEND_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainSendFiat_POST")
      .post(routes.SendFiatController.blockchainSendFiat().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.AMOUNT -> "${%s}".format(Test.TEST_AMOUNT),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Test.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("SUCCESS FIAT_SENT").exists)
    )
}