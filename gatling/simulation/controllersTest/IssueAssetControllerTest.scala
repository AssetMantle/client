package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

class IssueAssetControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = issueAssetControllerTest.issueAssetScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object issueAssetControllerTest {

  val issueAssetRequestScenario: ScenarioBuilder = scenario("IssueAssetRequest")
    .feed(DocumentHashFeeder.documentHashFeed)
    .feed(AssetTypeFeeder.assetTypeFeed)
    .feed(AssetPriceFeeder.assetPriceFeed)
    .feed(QuantityUnitFeeder.quantityUnitFeed)
    .feed(AssetQuantityFeeder.assetQuantityFeed)
    .exec(http("IssueAssetRequest_GET")
      .get(routes.IssueAssetController.issueAssetRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueAssetRequest_POST")
      .post(routes.IssueAssetController.issueAssetRequest().url)
      .formParamMap(Map(
        Form.DOCUMENT_HASH -> "${%s}".format(Test.TEST_DOCUMENT_HASH),
        Form.ASSET_TYPE -> "${%s}".format(Test.TEST_ASSET_TYPE),
        Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
        Form.QUANTITY_UNIT -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

  val rejectIssueAssetScenario: ScenarioBuilder = scenario("RejectIssueAsset")
    .feed(RequestIDFeeder.requestIDFeed)
    .exec(http("RejectIssueAsset_GET")
      .get(routes.IssueAssetController.rejectIssueAssetRequestForm(Test.TEST_REQUEST_ID).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RejectIssueAsset_POST")
      .post(routes.IssueAssetController.issueAssetRequest().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val issueAssetScenario: ScenarioBuilder = scenario("IssueAsset")
    .feed(RequestIDFeeder.requestIDFeed)
    .feed(AccountIDFeeder.accountIDFeed)
    .feed(DocumentHashFeeder.documentHashFeed)
    .feed(AssetTypeFeeder.assetTypeFeed)
    .feed(AssetPriceFeeder.assetPriceFeed)
    .feed(QuantityUnitFeeder.quantityUnitFeed)
    .feed(AssetQuantityFeeder.assetQuantityFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("IssueAsset_GET")
      .get(routes.IssueAssetController.issueAssetForm("l", "l", "l", "l", 1, "l", 1).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueAsset_POST")
      .post(routes.IssueAssetController.issueAsset().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ACCOUNT_ID),
        Form.DOCUMENT_HASH -> "${%s}".format(Test.TEST_DOCUMENT_HASH),
        Form.ASSET_TYPE -> "${%s}".format(Test.TEST_ASSET_TYPE),
        Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
        Form.QUANTITY_UNIT -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS), Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

  val blockchainIssueAssetScenario: ScenarioBuilder = scenario("BlockchainIssueAsset")
    .feed(FromFeeder.fromFeed)
    .feed(ToFeeder.toFeed)
    .feed(DocumentHashFeeder.documentHashFeed)
    .feed(AssetTypeFeeder.assetTypeFeed)
    .feed(AssetPriceFeeder.assetPriceFeed)
    .feed(QuantityUnitFeeder.quantityUnitFeed)
    .feed(AssetQuantityFeeder.assetQuantityFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainIssueAsset_GET")
      .get(routes.IssueAssetController.blockchainIssueAssetForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainIssueAsset_POST")
      .post(routes.IssueAssetController.blockchainIssueAsset().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.DOCUMENT_HASH -> "${%s}".format(Test.TEST_DOCUMENT_HASH),
        Form.ASSET_TYPE -> "${%s}".format(Test.TEST_ASSET_TYPE),
        Form.ASSET_PRICE -> "${%s}".format(Test.TEST_ASSET_PRICE),
        Form.QUANTITY_UNIT -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        Form.ASSET_QUANTITY -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

  def getRequestIDForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/comdex", "comdex", "comdex",
      s"""SELECT "id" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query';""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getPegHashByOwnerAddress(ownerAddress: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/comdex", "comdex", "comdex",
      s"""SELECT "pegHash" FROM blockchain."Asset_BC" WHERE "ownerAddress" = '$ownerAddress';""")
    sqlQueryFeeder.apply().next()("pegHash").toString
  }
}