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
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object issueFiatControllerTest {

  val issueFiatRequestScenario: ScenarioBuilder = scenario("IssueFiatRequest")
    .feed(TransactionIDFeeder.transactionIDFeed)
    .feed(TransactionAmountFeeder.transactionAmountFeed)
    .exec(http("Issue_Fiat_Request_GET")
      .get(routes.IssueFiatController.issueFiatRequestForm().url)
      .check(css("legend:contains(%s)".format("Issue Fiat Request")).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
   /* .exec(http("Issue_Fiat_Request_POST")
      .post(routes.IssueFiatController.issueFiatRequest().url)
      .formParamMap(Map(
        Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
        Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ISSUE_FIAT_REQUEST_SENT").exists)
    )*/
    .pause(3)

  val issueFiatScenario: ScenarioBuilder = scenario("IssueFiat")
    .feed(GasFeeder.gasFeed)
    .exec(session=> session.set(Test.TEST_REQUEST_ID,getRequestIDForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])).set(Test.TEST_TRANSACTION_ID,getTransactionIDForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])).set(Test.TEST_TRANSACTION_AMOUNT,getTransactionAmountForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])))
    .exec(http("Issue_Fiat_GET")
      .get(session=> routes.IssueFiatController.issueFiatForm(session(Test.TEST_REQUEST_ID).as[String],session(Test.TEST_BUYER_USERNAME).as[String],session(Test.TEST_TRANSACTION_ID).as[String],session(Test.TEST_TRANSACTION_AMOUNT).as[Int]).url)
      .check(css("legend:contains(%s)".format(constants.Form.ISSUE_FIAT.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Issue_Fiat_POST")
      .post(routes.IssueFiatController.issueFiat().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
        Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS FIAT_ISSUED").exists)
    )
    .pause(3)

   val rejectFiatRequestScenario: ScenarioBuilder = scenario("RejectFiatRequest")
    .exec(http("RejectIssueFiat_GET")
      .get(routes.IssueFiatController.rejectIssueFiatRequestForm(Test.TEST_REQUEST_ID).url)
      .check(css("legend:contains(%s)".format(constants.Form.REJECT_ISSUE_FIAT_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    /*.exec(http("RejectIssueFiat_POST")
      .post(routes.IssueFiatController.issueFiatRequest().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS ISSUE_FIAT_REQUEST_REJECTED").exists)
    )*/
     .pause(2)

  val blockchainIssueFiatScenario: ScenarioBuilder = scenario("BlockchainIssueFiat")
    .feed(FromFeeder.fromFeed)
    .feed(ToFeeder.toFeed)
    .feed(TransactionIDFeeder.transactionIDFeed)
    .feed(TransactionAmountFeeder.transactionAmountFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(ModeFeeder.modeFeed)
    .feed(GasFeeder.gasFeed)
    .exec(http("BlockchainIssueFiat_GET")
      .get(routes.IssueFiatController.blockchainIssueFiatForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.BLOCKCHAIN_ISSUE_FIAT.legend)).exists)
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
        Form.MODE ->"${%s}".format(Test.TEST_MODE),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS FIAT_ISSUED").exists)
    )

  def getRequestIDForIssueFiat(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master_transaction."IssueFiatRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getTransactionIDForIssueFiat(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "transactionID" FROM master_transaction."IssueFiatRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getTransactionAmountForIssueFiat(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "transactionAmount" FROM master_transaction."IssueFiatRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

}