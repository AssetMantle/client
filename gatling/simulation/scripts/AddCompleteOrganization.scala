package scripts

import constants.Test
import controllersTest._
import feeders.JDBCFeeder._
import feeders._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class AddCompleteOrganization extends Simulation {

  val zoneUsername=""
  val zonePassword="123123123"

  val addCompleteOrganizationScenario = scenario("AddCompleteOrganization")
    .exec { session => session.set(Test.TEST_ZONE_ID, getZoneID(zoneUsername)) }
    .feed(SellOrganizationLoginFeeder.sellOrganizationLoginFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELL_ORGANIZATION_PASSWORD).as[String]))
    .exec(AccountControllerTest.signUpScenario)
    .exec(AccountControllerTest.loginScenario)
    .exec(ContactControllerTest.addOrUpdateMobileNumberScenario)
    .exec(ContactControllerTest.verifyMobileNumberScenario)
    .exec(ContactControllerTest.addOrUpdateEmailAddressScenario)
    .exec(ContactControllerTest.verifyEmailAddressScenario)
    .exec(AccountControllerTest.addIdentification)
    .exec(AddOrganizationControllerTest.addOrganizationRequestScenario)
    .exec(AddOrganizationControllerTest.userAddUBO)
    .exec(AddOrganizationControllerTest.userAddUBO)
    .exec(AddOrganizationControllerTest.userDeleteUBO)
    .exec(AccountControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, zoneUsername).set(Test.TEST_PASSWORD,zonePassword))
    .exec(AccountControllerTest.loginScenario)
    .exec { session => session.set(Test.TEST_SELL_ORGANIZATION_ID, getOrganizationID(session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String])) }
    .exec { session => session.set(Test.TEST_ORGANIZATION_ID, session(Test.TEST_SELL_ORGANIZATION_ID).as[String]) }
    .exec(AddOrganizationControllerTest.rejectOrganizationRequestScenario)
    .exec(AccountControllerTest.logoutScenario)
    .pause(Test.BLOCKCHAIN_TRANSACTION_DELAY)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELL_ORGANIZATION_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELL_ORGANIZATION_PASSWORD).as[String]))
    .exec(AccountControllerTest.loginScenario)
    .exec(AddOrganizationControllerTest.addUBO)
    .exec(AddOrganizationControllerTest.deleteUBO)
    .exec(AddOrganizationControllerTest.addOrUpdateOrganizationBankAccount)
    .exec(AccountControllerTest.logoutScenario)

  setUp(
    addCompleteOrganizationScenario.inject(atOnceUsers(1))
  ).protocols(http.baseUrl(Test.BASE_URL))
}
