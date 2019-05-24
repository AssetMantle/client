package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class ConfirmBuyerBidControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = confirmBuyerBidControllerTest.confirmBuyerBidScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object confirmBuyerBidControllerTest {

  val confirmBuyerBidScenario: ScenarioBuilder = scenario("ConfirmBuyerBid")
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(BuyerBidFeeder.buyerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("ConfirmBuyerBid_GET")
      .get(routes.ConfirmBuyerBidController.confirmBuyerBidForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ConfirmBuyerBid_POST")
      .post(routes.ConfirmBuyerBidController.confirmBuyerBid().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.BID -> "${%s}".format(Test.TEST_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

  val blockchainConfirmBuyerBidScenario: ScenarioBuilder = scenario("BlockchainConfirmBuyerBid")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(BuyerBidFeeder.buyerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainConfirmBuyerBid_GET")
      .get(routes.ConfirmBuyerBidController.blockchainConfirmBuyerBidForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainConfirmBuyerBid_POST")
      .post(routes.ConfirmBuyerBidController.blockchainConfirmBuyerBid().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.BID -> "${%s}".format(Test.TEST_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
}