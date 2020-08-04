package simulations

import constants.Test
import scenarios._
import feeders.JDBCFeeder._
import feeders._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class RejectTrader extends Simulation {

  val organizationUsername=""
  val organizationPassword=""

  val rejectTraderScenario = scenario("RejectTrader")
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, getOrganizationID(organizationUsername)) }
    .feed(SellerFeeder.sellerFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.signUpScenario)
    .exec(AccountControllerTest.loginScenario)
    .exec(ContactControllerTest.addOrUpdateMobileNumberScenario)
    .exec(ContactControllerTest.verifyMobileNumberScenario)
    .exec(ContactControllerTest.addOrUpdateEmailAddressScenario)
    .exec(ContactControllerTest.verifyEmailAddressScenario)
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, session(Test.TEST_SELL_ORGANIZATION_ID).as[String]) }
    .exec(SetACLControllerTest.addTraderRequest)
    .exec(AccountControllerTest.logoutScenario)
    .exec { session => session.set(Test.TEST_SELLER_TRADER_ID, getTraderID(session(Test.TEST_SELLER_USERNAME).as[String])).set(Test.TEST_TRADER_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]) }
    .exec { session => session.set(Test.TEST_TRADER_ID, session(Test.TEST_SELLER_TRADER_ID).as[String]) }
    .exec(session => session.set(Test.TEST_USERNAME, organizationUsername).set(Test.TEST_PASSWORD, organizationPassword))
    .exec(AccountControllerTest.loginScenario)
    .exec(TraderControllerTest.organizationRejectTrader)

  setUp(
    rejectTraderScenario.inject(atOnceUsers(1))
  ).protocols(http.baseUrl(Test.BASE_URL))
}
