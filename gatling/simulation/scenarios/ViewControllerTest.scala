package scenarios

import constants.Test
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._


object ViewControllerTest {
  val blockScenario: ScenarioBuilder = scenario("Blocks Scenario")
    .exec(http("Block_Height_Scenario_GET")
      .get(routes.ViewController.block(height = Test.BLOCK_HEIGHT).url)
      .check(
        status.is(200),
        //10 : Total Number of componentViewController jsRoutes in a response
        regex("jsRoutes.controllers.ComponentViewController.*").count.is(10),
        regex("jsRoutes.controllers.ComponentViewController.block.*").exists,
        css("div[id=explorerContent]").notNull,
        // Left Containers
        css("div[id=latestBlockHeight]").exists,
        css("div[id=tokensStatistics]").exists,
        // Right Containers
        css("div[id=recentActivities]").exists,
        css("div[id=votingPowers]").exists,
        bodyString.saveAs("block"))
    ).pause(Test.REQUEST_DELAY)
    .exec(http("Block_Scenario_GET")
      .get(routes.ViewController.blocks().url)
      .check(
        regex("jsRoutes.controllers.ComponentViewController.*").count.is(10),
        css("div[id=explorerContent]").notNull,
        regex("jsRoutes.controllers.ComponentViewController.blockList()").count.is(1),
        css("div[id=latestBlockHeight]").exists,
        css("div[id=tokensStatistics]").exists,
        css("div[id=recentActivities]").exists,
        css("div[id=votingPowers]").exists,
        status.is(200),
        bodyString.saveAs("blocks"),
      )
    )
    .exec(session => {
      val response = session("blocks").as[String]
      val response2 = session("block").as[String]
      println(s"Response body block:\n $response2")
      session
    })

  val transactionsScenario: ScenarioBuilder = scenario("Transactions Scenario")
    .exec(http("Block_Scenario_GET")
      .get(routes.ViewController.transactions().url)
      .check(
        regex("jsRoutes.controllers.ComponentViewController.*").count.is(10),
        css("div[id=explorerContent]").notNull,
        regex("jsRoutes.controllers.ComponentViewController.transactionList()").count.is(2),
        // Left Containers
        css("div[id=latestBlockHeight]").exists,
        css("div[id=tokensStatistics]").exists,
        // Right Containers
        css("div[id=recentActivities]").exists,
        css("div[id=votingPowers]").exists,
        status.is(200),
        bodyString.saveAs("transactions"),
      )
    ).pause(Test.REQUEST_DELAY)
    .exec(http("Transaction_Hash_Scenario_GET")
      .get(routes.ViewController.transaction(txHash = Test.TRANSACTION_HASH).url)
      .check(
        regex("jsRoutes.controllers.ComponentViewController.*").count.is(10),
        regex("jsRoutes.controllers.ComponentViewController.transaction.*").count.is(2),
        css("div[id=explorerContent]").notNull,
        // Left Containers
        css("div[id=latestBlockHeight]").exists,
        css("div[id=tokensStatistics]").exists,
        // Right Containers
        css("div[id=recentActivities]").exists,
        css("div[id=votingPowers]").exists,
        status.is(200))
    ).pause(Test.REQUEST_DELAY)
    .exec(session => {
      val response1 = session("transactions").as[String]
      println(s"Response Body for Transactions:\n$response1")
      session
    })


  val proposalsScenario: ScenarioBuilder = scenario("Proposals Scenario")
    .exec(http("Proposals_Scenario_GET")
      .get(routes.ViewController.proposals().url)
      .check(
        regex("jsRoutes.controllers.ComponentViewController.*").count.is(10),
        css("div[id=explorerContent]").notNull,
        regex("jsRoutes.controllers.ComponentViewController.proposalList()").count.is(2),
        // Left Containers
        css("div[id=latestBlockHeight]").exists,
        css("div[id=tokensStatistics]").exists,
        // Right Containers
        css("div[id=recentActivities]").exists,
        css("div[id=votingPowers]").exists,
        bodyString.saveAs("proposals"),
        status.is(200),
      )
    ).pause(Test.REQUEST_DELAY)
    .exec(http("Proposals_Id_Scenario_GET")
      .get(routes.ViewController.proposal(proposalID = Test.TEST_PROPOSAL_ID).url)
      .check(
        regex("jsRoutes.controllers.ComponentViewController.*").count.is(10),
        regex("jsRoutes.controllers.ComponentViewController.proposal.*").count.is(2),
        css("div[id=explorerContent]").notNull,
        // Left Containers
        css("div[id=latestBlockHeight]").exists,
        css("div[id=tokensStatistics]").exists,
        // Right Containers
        css("div[id=recentActivities]").exists,
        css("div[id=votingPowers]").exists,
        bodyString.saveAs("proposal"),
        status.is(200),
      )
    ).pause(Test.REQUEST_DELAY)
    .exec(session => {
      val response = session("proposals").as[String]
      val response2 = session("proposal").as[String]
      println(s"Response body proposals:\n$response2")
      session
    })

  val validatorsScenario: ScenarioBuilder = scenario("Validators Scenario")
    .exec(http("Validators_Scenario_GET")
      .get(routes.ViewController.validators().url)
      .check(
        regex("jsRoutes.controllers.ComponentViewController.*").count.is(10),
        regex("jsRoutes.controllers.ComponentViewController.validatorList()").count.is(2),
        css("div[id=explorerContent]").notNull,
        // Left Containers
        css("div[id=latestBlockHeight]").exists,
        css("div[id=tokensStatistics]").exists,
        // Right Containers
        css("div[id=recentActivities]").exists,
        css("div[id=votingPowers]").exists,
        status.is(200),
      )
    ).pause(Test.REQUEST_DELAY)

    .exec(http("Validators_Address_Scenario_GET")
      .get(routes.ViewController.validator(address = Test.TEST_VALIDATOR_ADDRESS).url)
      .check(
        regex("jsRoutes.controllers.ComponentViewController.*").count.is(10),
        regex("jsRoutes.controllers.ComponentViewController.validator.*").count.is(2),
        css("div[id=explorerContent]").notNull,
        // Left Containers
        css("div[id=latestBlockHeight]").exists,
        css("div[id=tokensStatistics]").exists,
        // Right Containers
        css("div[id=recentActivities]").exists,
        css("div[id=votingPowers]").exists,
        status.is(200),
      )
    ).pause(Test.REQUEST_DELAY)
}