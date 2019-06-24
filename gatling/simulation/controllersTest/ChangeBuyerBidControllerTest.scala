package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

class ChangeBuyerBidControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = changeBuyerBidControllerTest.changeBuyerBidScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object changeBuyerBidControllerTest {

  val changeBuyerBidScenario: ScenarioBuilder = scenario("ChangeBuyerBid")
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(BuyerBidFeeder.buyerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("ChangeBuyerBid_GET")
      .get(routes.ChangeBuyerBidController.changeBuyerBidForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ChangeBuyerBid_POST")
      .post(routes.ChangeBuyerBidController.changeBuyerBid().url)
      .formParamMap(Map(
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.BID -> "${%s}".format(Test.TEST_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

  val blockchainChangeBuyerBidScenario: ScenarioBuilder = scenario("BlockchainChangeBuyerBid")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(BuyerBidFeeder.buyerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainChangeBuyerBid_GET")
      .get(routes.ChangeBuyerBidController.blockchainChangeBuyerBidForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainChangeBuyerBid_POST")
      .post(routes.ChangeBuyerBidController.blockchainChangeBuyerBid().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.BID -> "${%s}".format(Test.TEST_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

  def getBuyerAddress(buyerUsername: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/comdex", "comdex", "comdex",
      s"""SELECT COALESCE((SELECT "accountAddress" FROM master."Account" WHERE "id" = '$buyerUsername'),'0') AS "accountAddress";""")
    sqlQueryFeeder.apply().next()("accountAddress")
  }
}