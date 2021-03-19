/*
package controllersTest

import constants.{Form, Test}
import controllers.routes
import controllersTest.changeBuyerBidControllerTest.getAddressFromAccountID
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class RedeemAssetControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = redeemAssetControllerTest.redeemAssetScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object redeemAssetControllerTest {

  val redeemAssetScenario: ScenarioBuilder = scenario("RedeemAsset")
    .feed(GasFeeder.gasFeed)
    .exec(http("Redeem_Asset_GET")
      .get(session=> routes.RedeemAssetController.redeemAssetForm(session(Test.TEST_BUYER_USERNAME).as[String],session(Test.TEST_PEG_HASH).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.REDEEM_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Redeem_Asset_POST")
      .post(routes.RedeemAssetController.redeemAsset().url)
      .formParamMap(Map(
        Form.ZONE_ID -> "${%s}".format(Test.TEST_ZONE_ID),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD ->"${%s}".format(Test.TEST_BUYER_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ASSET_REDEEMED").exists)
    )
    .pause(3)


  val blockchainRedeemAssetScenario: ScenarioBuilder = scenario("BlockchainRedeemAsset")
    .feed(FromFeeder.fromFeed)
    .feed(ToFeeder.toFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ModeFeeder.modeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainRedeemAsset_GET")
      .get(routes.RedeemAssetController.blockchainRedeemAssetForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_REDEEM_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainRedeemAsset_POST")
      .post(routes.RedeemAssetController.blockchainRedeemAsset().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ASSET_REDEEMED").exists)
    )
}
*/
