package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef.jdbcFeeder

object issueFiatControllerTest {

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
      .disableFollowRedirect
      .check(status.is(302))
    )
    .pause(3)

  val westernUnionRTCB: ScenarioBuilder = scenario("westernUnionRTCB")
    .feed(wurtcbFeeder.wurtcbFeed)
    .exec { session => session.set(Test.TEST_EXTERNAL_REFRENCE, getRequestIDForIssueFiatRequest(session(Test.TEST_TRADER_ID).as[String])) }
    .exec(session=>
    session.set(Test.TEST_REQUEST_SIGNATURE,utilities.String.sha256Sum("D3M0r1c8KeyCoMd3X"+session(Test.TEST_ID).as[String]+session(Test.TEST_REFRENCE).as[String]+session(Test.TEST_EXTERNAL_REFRENCE).as[String]+session(Test.TEST_WU_INVOICE_NUMBER).as[String]+session(Test.TEST_BUYER_BUSINESS_ID).as[String]+session(Test.TEST_BUYER_FIRST_NAME).as[String]+session(Test.TEST_BUYER_LAST_NAME).as[String]+session(Test.TEST_CREATED_DATE).as[String]+session(Test.TEST_LAST_UPDATED_DATE).as[String]+session(Test.TEST_WU_STATUS).as[String]+session(Test.TEST_DEAL_TYPE).as[String]+session(Test.TEST_PAYMENT_TYPE_ID).as[String]+session(Test.TEST_TRANSACTION_AMOUNT).as[String]))
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
                         |<invoiceNumber>"""+session(Test.TEST_WU_INVOICE_NUMBER).as[String]+"""</invoiceNumber>
                         |
                         |<buyerBusinessId>"""+session(Test.TEST_BUYER_BUSINESS_ID).as[String]+"""</buyerBusinessId>
                         |
                         |<buyerFirstName>"""+session(Test.TEST_BUYER_FIRST_NAME).as[String]+"""</buyerFirstName>
                         |
                         |<buyerLastName>"""+session(Test.TEST_BUYER_LAST_NAME).as[String]+"""</buyerLastName>
                         |
                         |<createdDate>"""+session(Test.TEST_CREATED_DATE).as[String]+"""</createdDate>
                         |
                         |<lastUpdatedDate>"""+session(Test.TEST_LAST_UPDATED_DATE).as[String]+"""</lastUpdatedDate>
                         |
                         |<status>"""+session(Test.TEST_WU_STATUS).as[String]+"""</status>
                         |
                         |<dealType>"""+session(Test.TEST_DEAL_TYPE).as[String]+"""</dealType>
                         |
                         |<paymentTypeId>"""+session(Test.TEST_PAYMENT_TYPE_ID).as[String]+"""</paymentTypeId>
                         |
                         |<paidOutAmount>"""+session(Test.TEST_TRANSACTION_AMOUNT).as[String]+"""</paidOutAmount>
                         |
                         |<requestSignature>"""+session(Test.TEST_REQUEST_SIGNATURE).as[String]+"""</requestSignature>
                         |
                         |</request>""")).asXml
    )

  def getRequestIDForIssueFiatRequest(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://"+Test.TEST_IP+":5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM western_union."FiatRequest" WHERE "traderID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

}