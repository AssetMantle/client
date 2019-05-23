package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class RedeemAssetControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = redeemAssetControllerTest.redeemAssetScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object redeemAssetControllerTest {

  val redeemAssetScenario: ScenarioBuilder = scenario("RedeemAsset")
    .feed(ToFeeder.toFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("RedeemAsset_GET")
      .get(routes.RedeemAssetController.redeemAssetForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RedeemAsset_POST")
      .post(routes.RedeemAssetController.redeemAsset().url)
      .formParamMap(Map(
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

  val blockchainRedeemAssetScenario: ScenarioBuilder = scenario("BlockchainRedeemAsset")
    .feed(FromFeeder.fromFeed)
    .feed(ToFeeder.toFeed)
    .feed(PegHashFeeder.pegHashFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainRedeemAsset_GET")
      .get(routes.RedeemAssetController.blockchainRedeemAssetForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainRedeemAsset_POST")
      .post(routes.RedeemAssetController.blockchainRedeemAsset().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.PEG_HASH -> "${%s}".format(Test.TEST_PEG_HASH),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
}