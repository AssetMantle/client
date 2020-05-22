package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

import scala.util.Random

class AssetControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = assetControllerTest.issueAssetRequestScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object assetControllerTest {

  val issueAssetRequestScenario: ScenarioBuilder = scenario("IssueAssetRequest")
    .feed(AssetTypeFeeder.assetTypeFeed)
    .feed(AssetPriceFeeder.assetPriceFeed)
    .feed(QuantityUnitFeeder.quantityUnitFeed)
    .feed(AssetQuantityFeeder.assetQuantityFeed)
    .feed(GasFeeder.gasFeed)
    .feed(IssueAssetDetailFeeder.issueAssetDetailFeeder)
    .feed(ShippingDetailsFeeder.shippingDetailsFeeder)
    .exec(http("IssueAssetDetailForm_GET")
      .get(routes.AssetController.issueForm().url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueAssetRequest_POST")
      .post(routes.AssetController.issue().url)
      .formParamMap(Map(
        constants.FormField.ASSET_TYPE.name -> "${%s}".format(Test.TEST_ASSET_TYPE),
        constants.FormField.ASSET_QUANTITY.name -> "${%s}".format(Test.TEST_ASSET_QUANTITY),
        constants.FormField.QUANTITY_UNIT.name -> "${%s}".format(Test.TEST_QUANTITY_UNIT),
        constants.FormField.ASSET_PRICE.name -> "${%s}".format(Test.TEST_ASSET_PRICE),
        constants.FormField.SHIPPING_PERIOD.name -> "${%s}".format(Test.TEST_SHIPPING_PERIOD),
        constants.FormField.PORT_OF_LOADING.name -> "${%s}".format(Test.TEST_PORT_OF_LOADING),
        constants.FormField.PORT_OF_DISCHARGE.name -> "${%s}".format(Test.TEST_PORT_OF_DISCHARGE),
        constants.FormField.GAS.name -> "${%s}".format(Test.TEST_GAS),
        constants.FormField.PASSWORD.name -> "${%s}".format(Test.TEST_PASSWORD),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("UPLOAD OBL").exists)
    )
    .pause(3)


  val blockchainIssueAssetScenario: ScenarioBuilder = scenario("BlockchainIssueAsset")
    .feed(FromFeeder.fromFeed)
    .feed(ToFeeder.toFeed)
    .feed(DocumentHashFeeder.documentHashFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .feed(ModeFeeder.modeFeed)
    .feed(IssueAssetDetailFeeder.issueAssetDetailFeeder)
    .exec(http("BlockchainIssueAsset_GET")
      .get(routes.IssueAssetController.blockchainIssueAssetForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_ISSUE_ASSET.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
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
        Form.MODERATED -> Random.nextBoolean(),
        Form.TAKER_ADDRESS -> "${%s}".format(Test.TEST_TO),
        Test.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN))))

  def getRequestIDForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getDocumentHashForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "documentHash" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getAssetTypeForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "assetType" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getAssetPriceForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "assetPrice" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getQuantityUnitForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "quantityUnit" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getAssetQuantityForIssueAsset(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "assetQuantity" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getPegHashByOwnerAddress(accountID: String): String = {
    println(accountID)
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "pegHash" FROM master_transaction."IssueAssetRequest" WHERE "accountID" = '$accountID'),'0') AS "pegHash";""")
    sqlQueryFeeder.apply().next()("pegHash").toString
  }
}
