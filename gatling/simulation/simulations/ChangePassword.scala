package simulations

import constants.Test
import scenarios._
import feeders.JDBCFeeder._
import feeders._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class ChangePassword extends Simulation {

  setUp(
    changePassword.changePasswordScenario.inject(atOnceUsers(1))
  ).protocols(http.baseUrl(Test.BASE_URL))
}

object changePassword{

  val changePasswordScenario = scenario("ChangePassword")
    .feed(SellerLoginFeeder.sellerLoginFeed)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String]))
    .exec(AccountControllerTest.signUpScenario)
    .exec(AccountControllerTest.loginScenario)
    .exec(AccountControllerTest.changePassword)
    .exec(AccountControllerTest.logoutScenario)
    .exec(session => session.set(Test.TEST_USERNAME, session(Test.TEST_SELLER_USERNAME).as[String]).set(Test.TEST_PASSWORD, session(Test.TEST_SELLER_PASSWORD).as[String] + constants.Test.NEW_PASSOWRD_SUFFIX))
    .exec(AccountControllerTest.loginScenario)

}
