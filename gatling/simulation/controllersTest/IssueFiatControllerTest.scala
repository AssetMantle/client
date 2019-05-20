package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

class IssueFiatControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = issueFiatControllerTest.issueFiatScenario
  setUp(scenarioBuilder.inject(atOnceUsers(Test.NUMBER_OF_USERS))).protocols(http.baseUrl(Test.BASE_URL))
}

object issueFiatControllerTest {

  val issueFiatRequestScenario: ScenarioBuilder = scenario("IssueFiatRequest")
    .feed(TransactionIDFeeder.transactionIDFeed)
    .feed(TransactionAmountFeeder.transactionAmountFeed)
    .exec(http("IssueFiatRequest_GET")
      .get(routes.IssueFiatController.issueFiatRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueFiatRequest_POST")
      .post(routes.IssueFiatController.issueFiatRequest().url)
      .formParamMap(Map(
        Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
        Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val rejectFiatRequestScenario: ScenarioBuilder = scenario("RejectFiatRequest")
    .feed(RequestIDFeeder.requestIDFeed)
  /*
      .exec(http("RejectIssueFiat_GET")
        .get(routes.IssueFiatController.rejectIssueFiatRequestForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
      .pause(2)
      .exec(http("RejectIssueFiat_POST")
        .post(routes.IssueFiatController.issueFiatRequest().url)
        .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
         Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
      .pause(5)
  */

  val issueFiatScenario: ScenarioBuilder = scenario("IssueFiat")
    .feed(RequestIDFeeder.requestIDFeed)
    .feed(AccountIDFeeder.accountIDFeed)
    .feed(TransactionIDFeeder.transactionIDFeed)
    .feed(TransactionAmountFeeder.transactionAmountFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("IssueFiat_GET")
      .get(routes.IssueFiatController.issueFiatForm("l", "l", "l", 1).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("IssueFiat_POST")
      .post(routes.IssueFiatController.issueFiat().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ACCOUNT_ID),
        Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
        Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val blockchainIssueFiatScenario: ScenarioBuilder = scenario("BlockchainIssueFiat")
    .feed(FromFeeder.fromFeed)
    .feed(ToFeeder.toFeed)
    .feed(TransactionIDFeeder.transactionIDFeed)
    .feed(TransactionAmountFeeder.transactionAmountFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainIssueFiat_GET")
      .get(routes.IssueFiatController.blockchainIssueFiatForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainIssueFiat_POST")
      .post(routes.IssueFiatController.blockchainIssueFiat().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
        Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  def getRequestIDForIssueFiat(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/comdex", "comdex", "comdex",
      s"""SELECT "id" FROM master_transaction."IssueFiatRequest" WHERE "accountID" = '$query';""")
    sqlQueryFeeder.apply().next()("id").toString
  }

}