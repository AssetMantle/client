package controllersTest

import constants.{Form, Test}
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

class ExplorerTest extends Simulation {
  val scenarioBuilder: ScenarioBuilder =  explorerTest.testScenario
  setUp(scenarioBuilder.inject(atOnceUsers(1))).protocols(http.baseUrl(Test.BASE_URL))
}

object explorerTest {

  val pauseBetweenRequests= 2

  val testScenario: ScenarioBuilder = scenario("ExplorerTest")
    .exec(http("Index_GET")
      .get(routes.IndexController.index().url)
        .resources(
          http("Get Javascript Routes").get(routes.JavaScriptRoutesController.javascriptRoutes().url),
          http("Latest Block Height").get(routes.ComponentViewController.latestBlockHeight().url),
          http("Token Statistics").get(routes.ComponentViewController.tokensStatistics().url),
          http("Token Prices").get(routes.ComponentViewController.tokensPrices().url),
          http("Recent Activities").get(routes.ComponentViewController.recentActivities().url)
          .resources(
            http("Recent Activities Messages").get(routes.NotificationController.recentActivityMessages(1).url)
          ),
          http("Voting Powers").get(routes.ComponentViewController.votingPowers().url)
        )
        .notSilent
    )
    .pause(pauseBetweenRequests)
    .exec(http("Blocks_GET")
      .get(routes.ViewController.blocks().url)
      .resources(
        http("Get Javascript Routes").get(routes.JavaScriptRoutesController.javascriptRoutes().url),
        http("Latest Block Height").get(routes.ComponentViewController.latestBlockHeight().url),
        http("Token Statistics").get(routes.ComponentViewController.tokensStatistics().url),
        http("Block List").get(routes.ComponentViewController.blockList().url)
        .resources(
          http("Block List Page 1").get(routes.ComponentViewController.blockListPage(1).url)
        ),
        http("Recent Activities").get(routes.ComponentViewController.recentActivities().url)
          .resources(
            http("Recent Activities Messages").get(routes.NotificationController.recentActivityMessages(1).url)
          ),
        http("Voting Powers").get(routes.ComponentViewController.votingPowers().url)
      )
      .notSilent
    )
    .pause(pauseBetweenRequests)
    .exec(http("Transactions_GET")
      .get(routes.ViewController.transactions().url)
      .resources(
        http("Get Javascript Routes").get(routes.JavaScriptRoutesController.javascriptRoutes().url),
        http("Latest Block Height").get(routes.ComponentViewController.latestBlockHeight().url),
        http("Token Statistics").get(routes.ComponentViewController.tokensStatistics().url),
        http("Transaction List").get(routes.ComponentViewController.transactionList().url)
          .resources(
            http("Transaction List Page 1").get(routes.ComponentViewController.transactionListPage(1).url)
          ),
        http("Recent Activities").get(routes.ComponentViewController.recentActivities().url)
          .resources(
            http("Recent Activities Messages").get(routes.NotificationController.recentActivityMessages(1).url)
          ),
        http("Voting Powers").get(routes.ComponentViewController.votingPowers().url)
      )
      .notSilent
    )
    .pause(pauseBetweenRequests)
    .exec(http("Validators_GET")
      .get(routes.ViewController.validators().url)
      .resources(
        http("Get Javascript Routes").get(routes.JavaScriptRoutesController.javascriptRoutes().url),
        http("Latest Block Height").get(routes.ComponentViewController.latestBlockHeight().url),
        http("Token Statistics").get(routes.ComponentViewController.tokensStatistics().url),
        http("Validator List").get(routes.ComponentViewController.validatorList().url)
          .resources(
            http("Active Validators List").get(routes.ComponentViewController.activeValidatorList().url),
            http("Inactive Validators List").get(routes.ComponentViewController.inactiveValidatorList().url)
          ),
        http("Recent Activities").get(routes.ComponentViewController.recentActivities().url)
          .resources(
            http("Recent Activities Messages").get(routes.NotificationController.recentActivityMessages(1).url)
          ),
        http("Voting Powers").get(routes.ComponentViewController.votingPowers().url)
      )
      .notSilent
    )
    .pause(pauseBetweenRequests)
    .exec(http("Dashboard_GET")
      .get(routes.ViewController.dashboard().url)
      .resources(
        http("Get Javascript Routes").get(routes.JavaScriptRoutesController.javascriptRoutes().url),
        http("Latest Block Height").get(routes.ComponentViewController.latestBlockHeight().url),
        http("Token Statistics").get(routes.ComponentViewController.tokensStatistics().url),
        http("Token Prices").get(routes.ComponentViewController.tokensPrices().url),
        http("Recent Activities").get(routes.ComponentViewController.recentActivities().url)
          .resources(
            http("Recent Activities Messages").get(routes.NotificationController.recentActivityMessages(1).url)
          ),
        http("Voting Powers").get(routes.ComponentViewController.votingPowers().url)
      )
      .notSilent
    )
}