package scenarios

import constants.Test
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._


object ComponentViewControllerTest{
  val recentActivitiesScenario: ScenarioBuilder = scenario("Recent Activities Scenario")
    .exec(http("Recent_Activities_Scenario_GET")
      .get(routes.ComponentViewController.recentActivities().url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
    .exec(http("Common_Home_Scenario_GET")
      .get(routes.ComponentViewController.commonHome().url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
  val walletsScenario: ScenarioBuilder = scenario("Wallets Scenario")
    .exec(http("Wallet_Scenario_GET")
      .get(routes.ComponentViewController.wallet(address = Test.TEST_BLOCKCHAIN_ADDRESS).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
  val blockScenario: ScenarioBuilder = scenario("Block Height Scenario")
    .exec(http("Block_Height_Scenario_GET")
      .get(routes.ComponentViewController.block(height = Test.BLOCK_HEIGHT).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
    .exec(http("Latest_Block_Height_GET")
      .get(routes.ComponentViewController.latestBlockHeight().url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)

  val transactionScenario: ScenarioBuilder = scenario("Transaction Hash Scenario")
    .exec(http("Transaction_Hash_Scenario_GET")
      .get(routes.ComponentViewController.transaction(txHash = Test.TRANSACTION_HASH).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)

 val validatorScenario: ScenarioBuilder = scenario("Validator Scenario")
     .exec(http("Validators_Address_Scenario_GET")
      .get(routes.ComponentViewController.validator(address = Test.TEST_VALIDATOR_ADDRESS).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)

  val proposalScenario: ScenarioBuilder = scenario("Proposals Scenario")
    .exec(http("Proposal_Id_Scenario_GET")
      .get(routes.ComponentViewController.proposal(id = Test.TEST_PROPOSAL_ID).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
  val tokensScenario: ScenarioBuilder = scenario("Tokens Scenario")
    .exec(http("Tokens_Scenario_GET")
      .get(routes.ComponentViewController.tokensStatistics().url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
    .exec(http("Tokens_Prices_GET")
      .get(routes.ComponentViewController.tokensPrices().url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
  val VotingPowersScenario: ScenarioBuilder = scenario("Voting Powers Scenario")
    .exec(http("VotingPowers_Scenario_GET")
      .get(routes.ComponentViewController.votingPowers().url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
  val accountScenario: ScenarioBuilder = scenario("Account Wallet Address Scenario")
    .exec(http("AccountWallets_Scenario_GET")
      .get(routes.ComponentViewController.accountWallet(address = Test.TEST_ACCOUNT_ADDRESS).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
    .exec(http("AccountTransactions_Scenario_GET")
      .get(routes.ComponentViewController.accountDelegations(address = Test.TEST_ACCOUNT_ADDRESS).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)





  /*
  .exec(http("Validators_Scenario_GET")
     .get(routes.ViewController.validators().url)
     .check(status.is(200))
   ).pause(Test.REQUEST_DELAY)

*/

}