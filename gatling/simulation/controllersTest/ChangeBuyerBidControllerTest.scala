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
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object changeBuyerBidControllerTest {

  val changeBuyerBidScenario: ScenarioBuilder = scenario("ChangeBuyerBid")
  /*  .feed(BuyerBidFeeder.buyerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("ChangeBuyerBid_GET")
      .get(session=>routes.ChangeBuyerBidController.changeBuyerBidForm(session(Test.TEST_SELLER_ADDRESS).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.CHANGE_BUYER_BID.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      .check(css("[name=%s]".format(Form.REQUEST_ID), "value").saveAs(Form.REQUEST_ID))
    )
    .pause(2)
    .exec(http("Change_Buyer_Bid_POST")
      .post(routes.ChangeBuyerBidController.changeBuyerBid().url)
      .formParamMap(Map(
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN),
        Form.REQUEST_ID-> "${%s}".format(Form.REQUEST_ID),
        Form.SELLER_ADDRESS -> "${%s}".format(Test.TEST_SELLER_ADDRESS),
        Form.BID -> "${%s}".format(Test.TEST_BUYER_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Test.PASSWORD -> "${%s}".format(Test.TEST_BUYER_PASSWORD)))
        .check(substring("SUCCESS BUYER_BID_CHANGED").exists)
    )
    .pause(3)

  val blockchainChangeBuyerBidScenario: ScenarioBuilder = scenario("BlockchainChangeBuyerBid")
    .feed(FromFeeder.fromFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ToFeeder.toFeed)
    .feed(BuyerBidFeeder.buyerBidFeed)
    .feed(TimeFeeder.timeFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(ModeFeeder.modeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainChangeBuyerBid_GET")
      .get(routes.ChangeBuyerBidController.blockchainChangeBuyerBidForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_CHANGE_BUYER_BID.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN))
      )
    .pause(2)
    .exec(http("BlockchainChangeBuyerBid_POST")
      .post(routes.ChangeBuyerBidController.blockchainChangeBuyerBid().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.BID -> "${%s}".format(Test.TEST_BUYER_BID),
        Form.TIME -> "${%s}".format(Test.TEST_TIME),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Test.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("SUCCESS BUYER_BID_CHANGED").exists)
    )*/

  def getAddressFromAccountID(accountID: String):String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "accountAddress" FROM master."Account" WHERE "id" = '$accountID'),'0') AS "accountAddress";""")
    sqlQueryFeeder.apply().next()("accountAddress").toString
  }
}
