package scenarios

import constants.Test
import controllers.routes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._


object ComponentViewControllerTest {
  val recentActivitiesScenario: ScenarioBuilder = scenario("Recent Activities Scenario")
    .exec(http("Recent_Activities_Scenario_GET")
      .get(routes.ComponentViewController.recentActivities().url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)


  val walletsScenario: ScenarioBuilder = scenario("Wallets Scenario")
    .exec(http("Wallet_Scenario_GET")
      .get(routes.ComponentViewController.wallet(address = Test.TEST_BLOCKCHAIN_ADDRESS).url)
      .check(
        css("div[id =accountTransactions]").exists,
        css("div[id =accountDelegations]").exists,
        css("div[id =accountWallet]").exists,
        status.is(200),
        bodyString.saveAs("wallet")
      )
    ).pause(Test.REQUEST_DELAY)

  val blockScenario: ScenarioBuilder = scenario("Block Height Scenario")
    .exec(http("Block_Height_Scenario_GET")
      .get(routes.ComponentViewController.block(height = Test.BLOCK_HEIGHT).url)
      .check(
        substring(Test.BLOCK_HEIGHT.toString()).exists,
        bodyString.saveAs("blockHeight"),
        status.is(200),
      )
    ).pause(Test.REQUEST_DELAY)
  /*
      .exec(http("Latest_Block_Height_GET")
        .get(routes.ComponentViewController.latestBlockHeight().url)
        .check(status.is(200))
      ).pause(Test.REQUEST_DELAY)
  */
  val validatorScenario: ScenarioBuilder = scenario("Validator Scenario")
    .exec(http("Validators_Address_Scenario_GET")
      .get(routes.ComponentViewController.validator(address = Test.TEST_VALIDATOR_ADDRESS).url)
      .check(
        css("div[id =validatorDetails]").exists,
        bodyString.saveAs("Validator"),
        status.is(200),
      )
    ).pause(Test.REQUEST_DELAY)

  val proposalScenario: ScenarioBuilder = scenario("Proposals Scenario")
    .exec(http("Proposal_Id_Scenario_GET")
      .get(routes.ComponentViewController.proposal(id = Test.TEST_PROPOSAL_ID).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)

  val tokensScenario: ScenarioBuilder = scenario("Tokens Scenario")
    .exec(http("Tokens_Scenario_GET")
      .get(routes.ComponentViewController.tokensStatistics().url)
      .check(
        bodyString.saveAs("tokensScenario"),
        css("div[id =tokenStatistics_stake]").exists,
        status.is(200),
      )
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
    .exec(http("Account_Delegations_Scenario_GET")
      .get(routes.ComponentViewController.accountDelegations(address = Test.TEST_ACCOUNT_ADDRESS).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
    .exec(http("AccountTransactions_Scenario_GET")
      .get(routes.ComponentViewController.accountTransactions(address = Test.TEST_ACCOUNT_ADDRESS).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)

  val blockDetailsScenario: ScenarioBuilder = scenario("Block Details Scenario")
    .exec(http("BLOCK_DETAILS_GET")
      .get(routes.ComponentViewController.blockDetails(height = Test.BLOCK_HEIGHT).url)
      .check(
        substring(Test.BLOCK_HEIGHT.toString()).exists,
        bodyString.saveAs("blockDetails"),
        status.is(200),
      )
    ).pause(Test.REQUEST_DELAY)
    .exec(http("BLOCK_LIST_GET")
      .get(routes.ComponentViewController.blockList().url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)

  val failblockDetailsScenario: ScenarioBuilder = scenario("Fail Block Details Scenario")
    .exec(http("BLOCK_NOT_FOUND_FAILURE_GET")
      .get(routes.ComponentViewController.blockDetails(height = 2368249).url)
      .check(
        substring("RESPONSE.FAILURE.BLOCK_NOT_FOUND").exists,
        status.is(500),
      )
    ).pause(Test.REQUEST_DELAY)
  /*
    .exec(session => {
      // val response1 = session("BODY1").as[String]
      println(s"Response body: \n$response2")
      session
    })
  */


  val blockTransactionsScenario: ScenarioBuilder = scenario("Block Transactions Scenario")
    .exec(http("BLOCK_TRANSACTIONS_GET")
      .get(routes.ComponentViewController.blockTransactions(height = 14196).url)
      .check(
        status.is(200),
        substring(Test.TRANSACTION_HASH).exists,
        regex("""<td class="blockHeightItem">""").exists,
        regex("""<div class="hidden cmuk-position-center" id="BLOCK_TRANSACTIONS_LOADING" >""").exists,
      )
    ).pause(Test.REQUEST_DELAY)

  val failblockTransactionsScenario: ScenarioBuilder = scenario("Failed Block Transactions Scenario")
    .exec(http("BLOCK_TRANSACTIONS_FAIL_GET")
      .get(routes.ComponentViewController.blockTransactions(height = 1212124414).url)
      .check(
        bodyString.saveAs("failblockTransactions"),
        substring("No Transaction in the Block ").exists,
        status.is(200)
      )
    ).pause(Test.REQUEST_DELAY)
    .exec(session => {
      println(session("Validator").as[String])
      session
    })
  val transactionScenario: ScenarioBuilder = scenario("Transaction Details Scenario")
    .exec(http("Transaction_Hash_Scenario_GET")
      .get(routes.ComponentViewController.transaction(txHash = Test.TRANSACTION_HASH).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
    .exec(http("TRANSACTION_DETAILS_GET")
      .get(routes.ComponentViewController.transactionDetails(txHash = Test.TRANSACTION_HASH).url)
      .check(status.is(200))
    ).pause(Test.REQUEST_DELAY)
    .exec(http("TRANSACTION_HASH_FAILURE_GET")
      .get(routes.ComponentViewController.transactionMessages(txHash = Test.TEST_FAILED_TRANSACTION_HASH).url)
      .check(
        substring("RESPONSE.FAILURE.TRANSACTION_NOT_FOUND").exists,
        status.is(500)
      )
    ).pause(Test.REQUEST_DELAY)


}