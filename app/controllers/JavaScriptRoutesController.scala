package controllers

import controllers.actions.WithoutLoginAction
import play.api.mvc._
import play.api.routing._
import play.api.{Configuration, Logger}

import javax.inject.{Inject, Singleton}

@Singleton
class JavaScriptRoutesController @Inject()(messagesControllerComponents: MessagesControllerComponents, withoutLoginAction: WithoutLoginAction)(implicit configuration: Configuration) extends AbstractController(messagesControllerComponents) {

  private implicit val logger: Logger = Logger(this.getClass)

  def javascriptRoutes = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(
        JavaScriptReverseRouter("jsRoutes")(
          routes.javascript.Assets.versioned,

          routes.javascript.AccountController.signUpForm,
          routes.javascript.AccountController.signInForm,
          routes.javascript.AccountController.signOutForm,
          routes.javascript.AccountController.checkUsernameAvailable,
          routes.javascript.AccountController.updateProfileForm,
          routes.javascript.AccountController.updateSocialProfileForm,

          routes.javascript.AssetController.defineForm,
          routes.javascript.AssetController.mintForm,
          routes.javascript.AssetController.mutateForm,
          routes.javascript.AssetController.burnForm,

          routes.javascript.ChatController.chatRoom,
          routes.javascript.ChatController.chatWindow,
          routes.javascript.ChatController.sendMessageForm,
          routes.javascript.ChatController.loadMoreChats,
          routes.javascript.ChatController.replyToMessage,
          routes.javascript.ChatController.markChatAsRead,

          routes.javascript.ContactController.verifyEmailAddressForm,
          routes.javascript.ContactController.verifyMobileNumberForm,

          routes.javascript.ComponentViewController.dashboard,
          routes.javascript.ComponentViewController.block,
          routes.javascript.ComponentViewController.transaction,
          routes.javascript.ComponentViewController.validator,
          routes.javascript.ComponentViewController.wallet,
          routes.javascript.ComponentViewController.proposal,

          routes.javascript.ComponentViewController.commonHome,
          routes.javascript.ComponentViewController.profilePicture,
          routes.javascript.ComponentViewController.recentActivities,

          routes.javascript.ComponentViewController.latestBlockHeight,
          routes.javascript.ComponentViewController.tokensStatistics,
          routes.javascript.ComponentViewController.votingPowers,

          routes.javascript.ComponentViewController.tokensPrices,
          routes.javascript.ComponentViewController.transactionStatistics,

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

          routes.javascript.ComponentViewController.classification,
          routes.javascript.ComponentViewController.identity,
          routes.javascript.ComponentViewController.asset,
          routes.javascript.ComponentViewController.order,
          routes.javascript.ComponentViewController.meta,
          routes.javascript.ComponentViewController.maintainer,

          routes.javascript.ContactController.addOrUpdateEmailAddressForm,
          routes.javascript.ContactController.addOrUpdateMobileNumberForm,
          routes.javascript.ContactController.contact,

          routes.javascript.DocusignController.send,
          routes.javascript.DocusignController.sign,
          routes.javascript.DocusignController.authorization,

          routes.javascript.FileController.uploadAccountFileForm,
          routes.javascript.FileController.uploadAccountFile,
          routes.javascript.FileController.storeAccountFile,
          routes.javascript.FileController.updateAccountFileForm,
          routes.javascript.FileController.updateAccountFile,
          routes.javascript.FileController.file,

          routes.javascript.IdentityController.nubForm,
          routes.javascript.IdentityController.defineForm,
          routes.javascript.IdentityController.issueForm,
          routes.javascript.IdentityController.provisionForm,
          routes.javascript.IdentityController.unprovisionForm,

          routes.javascript.IndexController.search,

          routes.javascript.MetaController.revealForm,
          routes.javascript.MaintainerController.deputizeForm,

          routes.javascript.NotificationController.recentActivityMessages,
          routes.javascript.NotificationController.unreadNotificationCount,
          routes.javascript.NotificationController.markNotificationRead,

          routes.javascript.OrderController.defineForm,
          routes.javascript.OrderController.makeForm,
          routes.javascript.OrderController.takeForm,
          routes.javascript.OrderController.cancelForm,

          routes.javascript.SplitController.sendForm,
          routes.javascript.SplitController.wrapForm,
          routes.javascript.SplitController.unwrapForm,

          routes.javascript.TransactionsController.sendCoinForm,

          routes.javascript.ViewController.profile,
          routes.javascript.ViewController.identity,
          routes.javascript.ViewController.asset,
          routes.javascript.ViewController.order,
          routes.javascript.ViewController.classification,
          routes.javascript.ViewController.meta,
          routes.javascript.ViewController.maintainer,
          routes.javascript.ViewController.block,
          routes.javascript.ViewController.validator,
          routes.javascript.ViewController.transaction,
          routes.javascript.ViewController.proposal,
          routes.javascript.ViewController.blocks,
          routes.javascript.ViewController.transactions,
          routes.javascript.ViewController.validators,
          routes.javascript.ViewController.proposals,
          routes.javascript.ViewController.wallet,

          routes.javascript.BackgroundCheckController.memberScanForm,
          routes.javascript.BackgroundCheckController.memberScanResultDecisionForm,
          routes.javascript.BackgroundCheckController.addUBOMemberCheckForm,
          routes.javascript.BackgroundCheckController.memberScanResult,
          routes.javascript.BackgroundCheckController.corporateScanForm,
          routes.javascript.BackgroundCheckController.corporateScanResultDecisionForm,
          routes.javascript.BackgroundCheckController.addOrganizationMemberCheckForm,
          routes.javascript.BackgroundCheckController.corporateScanResult,
          routes.javascript.BackgroundCheckController.vesselScanForm,
          routes.javascript.BackgroundCheckController.vesselScanResultDecisionForm,
          routes.javascript.BackgroundCheckController.addAssetMemberCheckForm,
          routes.javascript.BackgroundCheckController.vesselScanResult,

        )
      ).as("text/javascript")
  }
}
