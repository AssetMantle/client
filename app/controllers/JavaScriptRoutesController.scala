package controllers

import controllers.actions.WithoutLoginAction
import play.api.mvc._
import play.api.routing._
import play.api.Configuration
import play.api.Logger

import javax.inject.{Inject, Singleton}

@Singleton
class JavaScriptRoutesController @Inject()(messagesControllerComponents: MessagesControllerComponents, withoutLoginAction: WithoutLoginAction)(implicit configuration: Configuration) extends AbstractController(messagesControllerComponents) {

  private implicit val logger: Logger = Logger(this.getClass)

  def javascriptRoutes = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(
        JavaScriptReverseRouter("jsRoutes")(
          routes.javascript.Assets.versioned,

          routes.javascript.ComponentViewController.dashboard,
          routes.javascript.ComponentViewController.block,
          routes.javascript.ComponentViewController.transaction,
          routes.javascript.ComponentViewController.validator,
          routes.javascript.ComponentViewController.wallet,
          routes.javascript.ComponentViewController.proposal,
          routes.javascript.ComponentViewController.document,

          routes.javascript.ComponentViewController.recentActivities,

          routes.javascript.ComponentViewController.latestBlockHeight,
          routes.javascript.ComponentViewController.tokensStatistics,
          routes.javascript.ComponentViewController.votingPowers,

          routes.javascript.ComponentViewController.tokensPrices,
          routes.javascript.ComponentViewController.transactionStatistics,
          routes.javascript.ComponentViewController.transactionMessagesStatistics,

          routes.javascript.ComponentViewController.accountWallet,
          routes.javascript.ComponentViewController.accountDelegations,
          routes.javascript.ComponentViewController.accountTransactions,
          routes.javascript.ComponentViewController.accountTransactionsPerPage,

          routes.javascript.ComponentViewController.blockList,
          routes.javascript.ComponentViewController.blockListPage,
          routes.javascript.ComponentViewController.blockDetails,
          routes.javascript.ComponentViewController.blockTransactions,

          routes.javascript.ComponentViewController.transactionList,
          routes.javascript.ComponentViewController.transactionListPage,
          routes.javascript.ComponentViewController.transactionDetails,
          routes.javascript.ComponentViewController.transactionMessages,
          routes.javascript.ComponentViewController.withdrawRewardAmount,

          routes.javascript.ComponentViewController.proposalList,
          routes.javascript.ComponentViewController.proposalDetails,
          routes.javascript.ComponentViewController.proposalDeposits,
          routes.javascript.ComponentViewController.proposalVotes,

          routes.javascript.ComponentViewController.validatorList,
          routes.javascript.ComponentViewController.activeValidatorList,
          routes.javascript.ComponentViewController.inactiveValidatorList,
          routes.javascript.ComponentViewController.validatorDetails,
          routes.javascript.ComponentViewController.validatorUptime,
          routes.javascript.ComponentViewController.validatorDelegations,
          routes.javascript.ComponentViewController.validatorTransactions,
          routes.javascript.ComponentViewController.validatorTransactionsPerPage,

          routes.javascript.IndexController.search,

          routes.javascript.NotificationController.recentActivityMessages,

          routes.javascript.ViewController.block,
          routes.javascript.ViewController.validator,
          routes.javascript.ViewController.transaction,
          routes.javascript.ViewController.proposal,
          routes.javascript.ViewController.blocks,
          routes.javascript.ViewController.transactions,
          routes.javascript.ViewController.validators,
          routes.javascript.ViewController.proposals,
          routes.javascript.ViewController.wallet,
          routes.javascript.ViewController.document,

        )
      ).as("text/javascript")
  }
}
