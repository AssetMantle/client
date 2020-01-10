package controllersTest

import constants.{Form, Test}
import controllers.routes
import feeders._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

import scala.concurrent.duration.Duration

class SendCoinControllerTest extends Simulation {

  val scenarioBuilder: ScenarioBuilder = sendCoinControllerTest.sendCoin
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object sendCoinControllerTest {

  val sendCoin=scenario("sendCoin")
    .exec(session=> session.set(Test.TEST_USERNAME,"testSendCoin123").set(Test.TEST_PASSWORD,"qwerty1234567890"))
    .exec(loginControllerTest.loginScenario)
    .repeat(12){
      exec(http("SendCoinController_GET")
        .get(routes.SendCoinController.sendCoinForm().url)
        .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
        .pause(2)
      .exec(http("Send Coin")
          .post(routes.SendCoinController.sendCoin().url)
          .formParamMap(Map(
            Form.TO -> "commit138wamrzhj2u8axgk9086zzu66hq9rmmnsv9yd2",
            Form.AMOUNT ->"10",
            Form.GAS -> "999999",
            Form.PASSWORD -> "qwerty1234567890",
            Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN))
          ))

    }

  val sendCoinScenario: ScenarioBuilder = scenario("SendCoin")
    .feed(AmountFeeder.amountFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec { session => {
      session.set(Test.TEST_TO, getAddress(session(Test.TEST_USERNAME).as[String]));
      session
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

  val sendCoinMainScenario: ScenarioBuilder = scenario("SendCoinMain")
    .feed(AmountFeeder.amountFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec { session => {
      session.set(Test.TEST_TO, getAddress(session(Test.TEST_MAIN_USERNAME).as[String]));
      session
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

  val blockchainSendCoinScenario: ScenarioBuilder = scenario("BlockchainSendCoin")
    .feed(FromFeeder.fromFeed)
    .feed(AmountFeeder.amountFeed)
    .feed(PasswordFeeder.passwordFeed)
    .feed(GasFeeder.gasFeed)
    .exec { session => {
      session.set(Test.TEST_TO, getAddress(session(Test.TEST_MAIN_USERNAME).as[String]));
      session
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

  val FaucetRequestScenario: ScenarioBuilder = scenario("FaucetRequest")
    .feed(CouponFeeder.couponFeed)
    .exec(http("FaucetRequest_GET")
      .get(routes.SendCoinController.faucetRequestForm().url)
      .check(css("legend:contains(%s)".format(constants.Form.FAUCET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Faucet_Request_POST")
      .post(routes.SendCoinController.faucetRequest().url)
      .formParamMap(Map(
        Form.COUPON -> "${%s}".format(Test.TEST_COUPON),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS COINS_REQUESTED").exists)
    )
    .pause(2)

  val rejectFaucetRequestScenario: ScenarioBuilder = scenario("RejectFaucetRequest")
    .exec(http("RejectFaucetRequest_GET")
      .get(routes.SendCoinController.rejectFaucetRequestForm(Test.TEST_REQUEST_ID).url)
      .check(css("legend:contains(%s)".format(constants.Form.REJECT_FAUCET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("RejectFaucetRequest_POST")
      .post(routes.SendCoinController.rejectFaucetRequest().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS FAUCET_REQUEST_REJECTED").exists)
    )
    .pause(3)

  val approveFaucetRequestScenario: ScenarioBuilder = scenario("ApproveFaucetRequest")
    .feed(GasFeeder.gasFeed)
    .exec(loginControllerTest.loginMain)
    .exec ( session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_USERNAME).as[String])) )
    .doIf(session=> session(Test.TEST_REQUEST_ID).as[String] == "0") {
      asLongAsDuring(session=> session(Test.TEST_REQUEST_ID).as[String] =="0",Duration.create(30,"seconds")) {
        pause(1)
          .exec { session => session.set(Test.TEST_REQUEST_ID, getRequestIDForFaucetRequest(session(Test.TEST_USERNAME).as[String])) }
      }
    }
    .exec(http("Approve_Faucet_Request_GET")
      .get(session=>routes.SendCoinController.approveFaucetRequestsForm(session(Test.TEST_REQUEST_ID).as[String],session(Test.TEST_USERNAME).as[String]).url)
      .check(css("legend:contains(%s)".format(constants.Form.APPROVE_FAUCET_REQUEST.legend)).exists)
      .check(css("[name=%s]".format(Form.CSRF_TOKEN), "value").saveAs(Form.CSRF_TOKEN)))
    .pause(2)
    .exec(http("Approve_Faucet_Request_POST")
      .post(routes.SendCoinController.approveFaucetRequests().url)
      .formParamMap(Map(
        Form.REQUEST_ID -> "${%s}".format(Test.TEST_REQUEST_ID),
        Form.ACCOUNT_ID -> "${%s}".format(Test.TEST_USERNAME),
        Form.PASSWORD ->  "${%s}".format(Test.TEST_MAIN_PASSWORD),
        Form.GAS -> "${%s}".format(Test.TEST_GAS),
        Form.CSRF_TOKEN -> "${%s}".format(Form.CSRF_TOKEN)))
      .check(substring("SUCCESS FAUCET_REQUEST_APPROVED").exists)
    )
    .pause(3)
    .exec(logoutControllerTest.logoutScenario)
    .exec{session => session.set(Test.USER_TYPE, getUserType(session(Test.TEST_USERNAME).as[String])) }
    .doIf(session=> session(Test.USER_TYPE).as[String] != "USER") {
      asLongAsDuring(session=> session(Test.USER_TYPE).as[String] != "USER",Duration.create(60,"seconds")) {
        pause(1)
          .exec { session =>session.set(Test.USER_TYPE, getUserType(session(Test.TEST_USERNAME).as[String])) }
      }
    }


  def getUserType(query: String):String={
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "userType" FROM master."Account" WHERE id = '$query'),'0') AS "userType";""")
       sqlQueryFeeder.apply().next()("userType").toString
  }

  def getAddress(query: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "accountAddress" FROM master."Account" WHERE id = '$query'),'0') AS "accountAddress";""")
    sqlQueryFeeder.apply().next()("accountAddress").toString
  }

  def getRequestIDForFaucetRequest(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://localhost:5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master_transaction."FaucetRequest" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }
}
