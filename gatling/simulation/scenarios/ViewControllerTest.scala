package scenarios

import constants.Test
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._


object ViewControllerTest {
  val blockScenario: ScenarioBuilder = scenario("Blocks Scenario")
    .exec(http("Block_Scenario_GET")
      .get(routes.ViewController.blocks().url)
      .check(
        status.is(200),
        bodyString.saveAs("blocks"),
      )
    ).exec(http("Block_Height_Scenario_GET")
    .get(routes.ViewController.block(height = Test.BLOCK_HEIGHT).url)
    .check(status.is(200))
  ).pause(Test.REQUEST_DELAY)

  val transactionsScenario: ScenarioBuilder = scenario("Transactions Scenario")
    .exec(http("Block_Scenario_GET")
      .get(routes.ViewController.transactions().url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
    .exec(http("Transaction_Hash_Scenario_GET")
      .get(routes.ViewController.transaction(txHash = Test.TRANSACTION_HASH).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)

  val proposalsScenario: ScenarioBuilder = scenario("Proposals Scenario")
    .exec(http("Proposals_Scenario_GET")
      .get(routes.ViewController.proposals().url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
    .exec(http("Proposals_Id_Scenario_GET")
      .get(routes.ViewController.proposal(proposalID = Test.TEST_PROPOSAL_ID).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)

  val validatorsScenario: ScenarioBuilder = scenario("Validators Scenario")
    .exec(http("Validators_Scenario_GET")
      .get(routes.ViewController.validators().url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
    .exec(http("Validators_Address_Scenario_GET")
      .get(routes.ViewController.validator(address = Test.TEST_VALIDATOR_ADDRESS).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)


}