package controllersTest

import constants.Test
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scenarios.{ComponentViewControllerTest, ViewControllerTest}


class ComponentViewControllerTest extends Simulation {

  val componentViewControllerTest=scenario("Component View Controller")
    .exec(ComponentViewControllerTest.recentActivitiesScenario)
    .exec(ComponentViewControllerTest.walletsScenario)
    .exec(ComponentViewControllerTest.blockScenario)
    .exec(ComponentViewControllerTest.transactionScenario)
    .exec(ComponentViewControllerTest.validatorScenario)
    .exec(ComponentViewControllerTest.proposalScenario)
    .exec(ComponentViewControllerTest.tokensScenario)
    .exec(ComponentViewControllerTest.VotingPowersScenario)
    .exec(ComponentViewControllerTest.accountScenario)



  setUp(
    componentViewControllerTest.inject(atOnceUsers(1)),
  ).protocols(http.baseUrl(Test.BASE_URL))




}
