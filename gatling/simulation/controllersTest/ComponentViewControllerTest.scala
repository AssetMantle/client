package controllersTest

import constants.Test
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scenarios.ComponentViewControllerTest


class ComponentViewControllerTest extends Simulation {

  val componentViewControllerTest = scenario("Component View Controller")
    .exec(ComponentViewControllerTest.walletsScenario)
    .exec(ComponentViewControllerTest.blockScenario)
    .exec(ComponentViewControllerTest.validatorScenario)
    .exec(ComponentViewControllerTest.blockDetailsScenario)
    .exec(ComponentViewControllerTest.tokensScenario)
    .exec(ComponentViewControllerTest.failblockTransactionsScenario)
    .exec(ComponentViewControllerTest.recentActivitiesScenario)
    .exec(ComponentViewControllerTest.proposalScenario)
    .exec(ComponentViewControllerTest.transactionScenario)
    .exec(ComponentViewControllerTest.VotingPowersScenario)
    .exec(ComponentViewControllerTest.accountScenario)
    .exec(ComponentViewControllerTest.blockTransactionsScenario)
    .exec(ComponentViewControllerTest.transactionScenario)
    .exec(ComponentViewControllerTest.failblockDetailsScenario)

  setUp(
    componentViewControllerTest.inject(atOnceUsers(1)),
  ).protocols(http.baseUrl(Test.BASE_URL))


}
