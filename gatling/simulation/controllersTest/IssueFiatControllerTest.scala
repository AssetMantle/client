package controllersTest

import java.util.Base64

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

class IssueFiatControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = issueFiatControllerTest.westernUnionRTCB
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object issueFiatControllerTest {

  val rtcbSecretKey="D3M0r1c8KeyCoMd3X"
  val id="10000123451"
  val refrence="CSGSGPDEMO0123461"
  val invoiceNumber="INV123456"
  val buyerBusinessID="STU123456"
  val buyerFirstName="John"
  val buyerLastName="Doe"
  val createdDate="2019-04-01 06:00:00"
  val lastUpdatedDate="2019-04-01 17:30:15"
  val wu_status="DEAL_POSTED"
  val dealType="Sale"
  val paymentTypeId="WIRE"
  val paidOutAmount="550000"

  val issueFiatRequestScenario: ScenarioBuilder = scenario("IssueFiatRequest")
    .feed(TransactionAmountFeeder.transactionAmountFeed)
    .exec(http("Issue_Fiat_Request_Form_GET")
      .get(routes.IssueFiatController.issueFiatRequestForm().url)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("westernUnionPortalRedirect_POST")
      .post(routes.WesternUnionController.westernUnionPortalRedirect().url)
      .formParamMap(Map(
        constants.FormField.TRANSACTION_AMOUNT.name -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(status.is(302))
    )
    .pause(3)

  val westernUnionRTCB: ScenarioBuilder = scenario("westernUnionRTCB")
    .feed(wurtcbFeeder.wurtcbFeed)
    .exec { session => session.set(Test.TEST_EXTERNAL_REFRENCE, getRequestIDForIssueFiatRequest(session(Test.TEST_TRADER_ID).as[String])) }
    .exec(session=>
    session.set("requestSignature",utilities.String.sha256Sum(rtcbSecretKey+session(Test.TEST_ID).as[String]+session(Test.TEST_REFRENCE).as[String]+session(Test.TEST_EXTERNAL_REFRENCE).as[String]+invoiceNumber+buyerBusinessID+buyerFirstName+buyerLastName+createdDate+lastUpdatedDate+wu_status+dealType+paymentTypeId+session(Test.TEST_TRANSACTION_AMOUNT).as[String]))
    )
    .exec(http("westernUnionRTCB")
      .post(routes.WesternUnionController.westernUnionRTCB().url)
      .body(StringBody(session=>s"""<request>
                         |
                         |<id>"""+session(Test.TEST_ID).as[String]+"""</id>
                         |
                         |<reference>"""+session(Test.TEST_REFRENCE).as[String]+"""</reference>
                         |
                         |<externalReference>"""+session(Test.TEST_EXTERNAL_REFRENCE).as[String]+"""</externalReference>
                         |
                         |<invoiceNumber>"""+invoiceNumber+"""</invoiceNumber>
                         |
                         |<buyerBusinessId>"""+buyerBusinessID+"""</buyerBusinessId>
                         |
                         |<buyerFirstName>"""+buyerFirstName+"""</buyerFirstName>
                         |
                         |<buyerLastName>"""+buyerLastName+"""</buyerLastName>
                         |
                         |<createdDate>"""+createdDate+"""</createdDate>
                         |
                         |<lastUpdatedDate>"""+lastUpdatedDate+"""</lastUpdatedDate>
                         |
                         |<status>"""+wu_status+"""</status>
                         |
                         |<dealType>"""+dealType+"""</dealType>
                         |
                         |<paymentTypeId>"""+paymentTypeId+"""</paymentTypeId>
                         |
                         |<paidOutAmount>"""+session(Test.TEST_TRANSACTION_AMOUNT).as[String]+"""</paidOutAmount>
                         |
                         |<requestSignature>"""+session("requestSignature").as[String]+"""</requestSignature>
                         |
                         |</request>""")).asXml
      /*  .transferEncoding("binary")).asXml)*/
    )

 /* val issueFiatScenario: ScenarioBuilder = scenario("IssueFiat")
    .feed(GasFeeder.gasFeed)
    .exec(session => session.set(Test.TEST_REQUEST_ID, getRequestIDForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])).set(Test.TEST_TRANSACTION_ID, getTransactionIDForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])).set(Test.TEST_TRANSACTION_AMOUNT, getTransactionAmountForIssueFiat(session(Test.TEST_BUYER_USERNAME).as[String])))
    .exec(http("Issue_Fiat_GET")
      .get(session => routes.IssueFiatController.issueFiatForm(session(Test.TEST_REQUEST_ID).as[String], session(Test.TEST_BUYER_USERNAME).as[String], session(Test.TEST_TRANSACTION_ID).as[String], session(Test.TEST_TRANSACTION_AMOUNT).as[Int]).url)
      .check(css("legend:contains(%s)".format(constants.Form.ISSUE_FIAT.legend)).exists)
      .check(css("[name=%s]".format(Test.CSRF_TOKEN), "value").saveAs(Test.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Issue_Fiat_POST")
      .post(routes.IssueFiatController.issueFiat().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_BUYER_USERNAME),
        Form.TRANSACTION_ID -> "${%s}".format(Test.TEST_TRANSACTION_ID),
        Form.TRANSACTION_AMOUNT -> "${%s}".format(Test.TEST_TRANSACTION_AMOUNT),
        Test.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Test.CSRF_TOKEN -> "${%s}".format(Test.CSRF_TOKEN)))
      .check(substring("SUCCESS FIAT_ISSUED").exists)
    )
    .pause(3)
  */
  def getRequestIDForIssueFiatRequest(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM western_union."FiatRequest" WHERE "traderID" = '$query'),'0') AS "id";""")
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