package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class SendCoinControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = sendCoinControllerTest.sendCoinScenario
  setUp(scenarioBuilder.inject(atOnceUsers(3))).protocols(http.baseUrl(Test.BASE_URL))
}

object sendCoinControllerTest {

  val sendCoinScenario: ScenarioBuilder = scenario("SendCoin")
    .feed(AmountFeeder.amountFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec { session => {
      session.set(Test.TEST_TO, getAddress(session(Test.TEST_USERNAME).as[String])); session
    }
    } //toFeed
    .exec(http("SendCoinController_GET")
    .get(routes.SendCoinController.sendCoinForm().url)
    .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("SendCoinController_POST")
      .post(routes.SendCoinController.sendCoin().url)
      .formParamMap(Map(
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.AMOUNT -> "${%s}".format(Test.TEST_AMOUNT),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val sendCoinMainScenario: ScenarioBuilder = scenario("SendCoinMain")
    .feed(AmountFeeder.amountFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec { session => {
      session.set(Test.TEST_TO, getAddress(session(Test.TEST_MAIN_USERNAME).as[String])); session
    }
    } //toFeed
    .exec(http("SendCoinMainController_GET")
    .get(routes.SendCoinController.sendCoinForm().url)
    .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("SendCoinMainController_POST")
      .post(routes.SendCoinController.sendCoin().url)
      .formParamMap(Map(
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.AMOUNT -> "${%s}".format(Test.TEST_AMOUNT),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val blockchainSendCoinScenario: ScenarioBuilder = scenario("BlockchainSendCoin")
    .feed(FromFeeder.fromFeed)
    .feed(AmountFeeder.amountFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec { session => {
      session.set(Test.TEST_TO, getAddress(session(Test.TEST_MAIN_USERNAME).as[String])); session
    }
    } //toFeed
    .exec(http("BlockchainSendCoinController_GET")
    .get(routes.SendCoinController.blockchainSendCoinForm().url)
    .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainSendCoinController_POST")
      .post(routes.SendCoinController.blockchainSendCoin().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.AMOUNT -> "${%s}".format(Test.TEST_AMOUNT),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val blockchainSendCoinMainScenario: ScenarioBuilder = scenario("BlockchainSendCoinMain")
    .feed(FromFeeder.fromFeed)
    .feed(AmountFeeder.amountFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec { session =>
      session.setAll(
        Test.TEST_TO -> getAddress(session(Test.TEST_USERNAME).as[String]),
        Test.TEST_FROM -> session(Test.TEST_MAIN_USERNAME).as[String],
        Test.TEST_PASSWORD -> session(Test.TEST_MAIN_PASSWORD).as[String])
    } //toFeed
    .pause(5)
    .exec(http("BlockchainSendCoinMainController_GET")
      .get(routes.SendCoinController.blockchainSendCoinForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("BlockchainSendCoinMainController_POST")
      .post(routes.SendCoinController.blockchainSendCoin().url)
      .formParamMap(Map(
        Form.FROM -> "${%s}".format(Test.TEST_FROM),
        Form.TO -> "${%s}".format(Test.TEST_TO),
        Form.AMOUNT -> "${%s}".format(Test.TEST_AMOUNT),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  val requestCoinScenario: ScenarioBuilder = scenario("RequestCoin")
    .feed(CouponFeeder.couponFeed)
    .exec(http("RequestCoin_GET")
      .get(routes.SendCoinController.requestCoinsForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RequestCoin_POST")
      .post(routes.SendCoinController.requestCoins().url)
      .formParamMap(Map(
        Form.COUPON -> "${%s}".format(Test.TEST_COUPON),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))

  val rejectFaucetRequestScenario: ScenarioBuilder = scenario("RejectFaucetRequest")
    .feed(RequestIDFeeder.requestIDFeed)

  val approveFaucetRequestScenario: ScenarioBuilder = scenario("ApproveFaucetRequest")
    .feed(AccountIDFeeder.accountIDFeed)
    .feed(RequestIDFeeder.requestIDFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec { session => session.set(Test.TEST_REQUEST_ID,getRequestID(session(Test.TEST_ACCOUNT_ID).as[String]))}
    .exec { session =>
      println(session)
      session
    }
    .exec(http("ApproveFaucetRequest_GET")
      .get(routes.SendCoinController.approveFaucetRequestsForm("${%s}".format(Test.TEST_REQUEST_ID),"${%s}".format(Test.TEST_ACCOUNT_ID)).url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("ApproveFaucetRequest_POST")
      .post(routes.SendCoinController.approveFaucetRequests().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_ACCOUNT_ID),
        Form.PASSWORD -> "${%s}".format(Test.TEST_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)

  def getAddress(query: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/comdex", "comdex", "comdex",
      s"""SELECT "accountAddress" FROM master."Account" WHERE id = '$query';""")
    sqlQueryFeeder.apply().next()("accountAddress")
  }
  /*
    .exec(http("RejectFaucetRequest_GET")
      .get(routes.SendCoinController.rejectFaucetRequestForm().url)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RejectFaucetRequest_POST")
      .post(routes.SendCoinController.rejectFaucetRequest().url)
      .formParamMap(Map(
      Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
       Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))))
    .pause(5)
*/

  def getRequestID(query: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/comdex", "comdex", "comdex",
      s"""SELECT "id" FROM master_transaction."FaucetRequest" WHERE "accountID" = '$query';""")
    sqlQueryFeeder.apply().next()("id")
  }
}