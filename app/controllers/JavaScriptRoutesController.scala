package controllers

import controllers.actions.WithoutLoginAction
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.routing._
import play.api.{Configuration, Logger}

@Singleton
class JavaScriptRoutesController @Inject()(messagesControllerComponents: MessagesControllerComponents, withoutLoginAction: WithoutLoginAction)(implicit configuration: Configuration) extends AbstractController(messagesControllerComponents) {

  private implicit val logger: Logger = Logger(this.getClass)

  def javascriptRoutes = withoutLoginAction { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.AccountController.signUpForm,
        routes.javascript.AccountController.loginForm,
        routes.javascript.AccountController.logoutForm,
        routes.javascript.AccountController.changePasswordForm,
        routes.javascript.AccountController.emailOTPForgotPasswordForm,
        routes.javascript.AccountController.forgotPasswordForm,
        routes.javascript.AccountController.checkUsernameAvailable,
        routes.javascript.AccountController.createWalletForm,
        routes.javascript.AccountController.addIdentificationForm,
        routes.javascript.AccountController.userViewUploadOrUpdateIdentification,
        routes.javascript.AccountController.userReviewIdentificationForm,
        routes.javascript.AccountController.checkMnemonics,
        routes.javascript.AccountController.importWalletForm,

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

        routes.javascript.ComponentViewController.commonHome,
        routes.javascript.ComponentViewController.profilePicture,
        routes.javascript.ComponentViewController.identification,
        routes.javascript.ComponentViewController.recentActivities,
        routes.javascript.ComponentViewController.publicRecentActivities,

        routes.javascript.ComponentViewController.latestBlockHeight,
        routes.javascript.ComponentViewController.tokensStatistics,
        routes.javascript.ComponentViewController.votingPowers,

        routes.javascript.ComponentViewController.tokensPrices,

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

        routes.javascript.ComponentViewController.validatorList,
        routes.javascript.ComponentViewController.activeValidatorList,
        routes.javascript.ComponentViewController.inactiveValidatorList,
        routes.javascript.ComponentViewController.validatorDetails,
        routes.javascript.ComponentViewController.validatorUptime,
        routes.javascript.ComponentViewController.validatorDelegations,
        routes.javascript.ComponentViewController.validatorTransactions,
        routes.javascript.ComponentViewController.validatorTransactionsPerPage,

        routes.javascript.ComponentViewController.entityProperties,

        routes.javascript.ComponentViewController.identitiesDefinition,
        routes.javascript.ComponentViewController.identitiesProvisioned,
        routes.javascript.ComponentViewController.identitiesUnprovisioned,
        routes.javascript.ComponentViewController.assetsDefinition,
        routes.javascript.ComponentViewController.assetsMinted,
        routes.javascript.ComponentViewController.ordersDefinition,
        routes.javascript.ComponentViewController.ordersMade,
        routes.javascript.ComponentViewController.ordersTake,

        routes.javascript.ContactController.addOrUpdateEmailAddressForm,
        routes.javascript.ContactController.addOrUpdateMobileNumberForm,
        routes.javascript.ContactController.contact,

        routes.javascript.DocusignController.send,
        routes.javascript.DocusignController.sign,
        routes.javascript.DocusignController.authorization,

        routes.javascript.EntityController.addLabelForm,
        routes.javascript.EntityController.addPrivatePropertyForm,

        routes.javascript.FileController.uploadAccountFileForm,
        routes.javascript.FileController.uploadAccountFile,
        routes.javascript.FileController.storeAccountFile,
        routes.javascript.FileController.updateAccountFileForm,
        routes.javascript.FileController.updateAccountFile,
        routes.javascript.FileController.file,
        routes.javascript.FileController.uploadAccountKYCForm,
        routes.javascript.FileController.uploadAccountKYC,
        routes.javascript.FileController.storeAccountKYC,
        routes.javascript.FileController.updateAccountKYCForm,
        routes.javascript.FileController.updateAccountKYC,

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
        routes.javascript.NotificationController.publicRecentActivityMessages,

        routes.javascript.SendCoinController.sendCoinForm,

        routes.javascript.ContactController.verifyEmailAddressForm,

        routes.javascript.ContactController.verifyMobileNumberForm,

        routes.javascript.OrderController.defineForm,
        routes.javascript.OrderController.makeForm,
        routes.javascript.OrderController.takeForm,
        routes.javascript.OrderController.cancelForm,

        routes.javascript.SplitController.sendForm,
        routes.javascript.SplitController.wrapForm,
        routes.javascript.SplitController.unwrapForm,

        routes.javascript.ViewController.profile,
        routes.javascript.ViewController.dashboard,
        routes.javascript.ViewController.block,
        routes.javascript.ViewController.transaction,
        routes.javascript.ViewController.validator,

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
