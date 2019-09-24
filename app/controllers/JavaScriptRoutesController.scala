package controllers

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc._
import play.api.routing._

@Singleton
class JavaScriptRoutesController @Inject()(messagesControllerComponents: MessagesControllerComponents)(implicit configuration: Configuration) extends AbstractController(messagesControllerComponents) {
  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.AccountController.signUpForm,
        routes.javascript.AccountController.loginForm,
        routes.javascript.AccountController.logoutForm,
        routes.javascript.AccountController.changePasswordForm,
        routes.javascript.AccountController.emailOTPForgotPasswordForm,
        routes.javascript.AccountController.forgotPasswordForm,
        routes.javascript.AccountController.checkUsernameAvailable,
        routes.javascript.AccountController.noteNewKeyDetails,

        routes.javascript.AddKeyController.blockchainAddKeyForm,

        routes.javascript.AddOrganizationController.addOrganizationForm,
        routes.javascript.AddOrganizationController.userUpdateUBOsForm,
        routes.javascript.AddOrganizationController.organizationBankAccountDetailForm,
        routes.javascript.AddOrganizationController.userUploadOrUpdateOrganizationKYCView,
        routes.javascript.AddOrganizationController.userUploadOrganizationKYCForm,
        routes.javascript.AddOrganizationController.userUploadOrganizationKYC,
        routes.javascript.AddOrganizationController.userStoreOrganizationKYC,
        routes.javascript.AddOrganizationController.userUpdateOrganizationKYCForm,
        routes.javascript.AddOrganizationController.userUpdateOrganizationKYC,
        routes.javascript.AddOrganizationController.reviewOrganizationCompletionForm,
        routes.javascript.AddOrganizationController.viewPendingVerifyOrganizationRequests,
        routes.javascript.AddOrganizationController.viewOrganizationVerificationBankAccountDetail,
        routes.javascript.AddOrganizationController.viewKYCDocuments,
        routes.javascript.AddOrganizationController.verifyKYCDocument,
        routes.javascript.AddOrganizationController.rejectKYCDocument,
        routes.javascript.AddOrganizationController.verifyOrganizationForm,
        routes.javascript.AddOrganizationController.rejectVerifyOrganizationRequestForm,
        routes.javascript.AddOrganizationController.viewOrganizationsInZone,
        routes.javascript.AddOrganizationController.viewOrganizationsInZoneForGenesis,
        routes.javascript.AddOrganizationController.blockchainAddOrganizationForm,
        routes.javascript.AddOrganizationController.uploadOrganizationKYCForm,
        routes.javascript.AddOrganizationController.uploadOrganizationKYC,
        routes.javascript.AddOrganizationController.storeOrganizationKYC,
        routes.javascript.AddOrganizationController.updateOrganizationKYCForm,
        routes.javascript.AddOrganizationController.updateOrganizationKYC,

        routes.javascript.AddZoneController.viewPendingVerifyZoneRequests,
        routes.javascript.AddZoneController.addZoneForm,
        routes.javascript.AddZoneController.userUploadOrUpdateZoneKYCView,
        routes.javascript.AddZoneController.reviewZoneCompletionForm,
        routes.javascript.AddZoneController.verifyZoneForm,
        routes.javascript.AddZoneController.rejectVerifyZoneRequestForm,
        routes.javascript.AddZoneController.viewZonesInGenesis,
        routes.javascript.AddZoneController.blockchainAddZoneForm,
        routes.javascript.AddZoneController.viewKYCDocuments,
        routes.javascript.AddZoneController.verifyKYCDocument,
        routes.javascript.AddZoneController.rejectKYCDocument,
        routes.javascript.AddZoneController.userUploadZoneKYCForm,
        routes.javascript.AddZoneController.userUploadZoneKYC,
        routes.javascript.AddZoneController.userStoreZoneKYC,
        routes.javascript.AddZoneController.userUpdateZoneKYCForm,
        routes.javascript.AddZoneController.userUpdateZoneKYC,
        routes.javascript.AddZoneController.uploadZoneKYCForm,
        routes.javascript.AddZoneController.uploadZoneKYC,
        routes.javascript.AddZoneController.storeZoneKYC,
        routes.javascript.AddZoneController.updateZoneKYCForm,
        routes.javascript.AddZoneController.updateZoneKYC,

        routes.javascript.BlockExplorerController.lastBlockHeight,
        routes.javascript.BlockExplorerController.blockDetails,
        routes.javascript.BlockExplorerController.stakingValidators,
        routes.javascript.BlockExplorerController.transactionHash,

        routes.javascript.BuyerExecuteOrderController.buyerExecuteOrderForm,
        routes.javascript.BuyerExecuteOrderController.moderatedBuyerExecuteOrderForm,
        routes.javascript.BuyerExecuteOrderController.moderatedBuyerExecuteOrderList,
        routes.javascript.BuyerExecuteOrderController.blockchainBuyerExecuteOrderForm,

        routes.javascript.ChangeBuyerBidController.changeBuyerBidForm,
        routes.javascript.ChangeBuyerBidController.blockchainChangeBuyerBidForm,

        routes.javascript.ChangeSellerBidController.changeSellerBidForm,
        routes.javascript.ChangeSellerBidController.blockchainChangeSellerBidForm,

        routes.javascript.ComponentViewController.commonHome,
        routes.javascript.ComponentViewController.genesisDetails,
        routes.javascript.ComponentViewController.zoneDetails,
        routes.javascript.ComponentViewController.organizationDetails,
        routes.javascript.ComponentViewController.assetList,
        routes.javascript.ComponentViewController.fiatList,
        routes.javascript.ComponentViewController.buyNegotiationList,
        routes.javascript.ComponentViewController.sellNegotiationList,
        routes.javascript.ComponentViewController.orderList,
        routes.javascript.ComponentViewController.availableAssetList,
        routes.javascript.ComponentViewController.availableAssetListWithLogin,
        routes.javascript.ComponentViewController.organizationViewTraderList,
        routes.javascript.ComponentViewController.organizationViewTrader,
        routes.javascript.ComponentViewController.profileDocuments,
        routes.javascript.ComponentViewController.profilePicture,

        routes.javascript.ConfirmBuyerBidController.confirmBuyerBidForm,
        routes.javascript.ConfirmBuyerBidController.blockchainConfirmBuyerBidForm,

        routes.javascript.ConfirmSellerBidController.confirmSellerBidForm,
        routes.javascript.ConfirmSellerBidController.blockchainConfirmSellerBidForm,

        routes.javascript.ConfigurationController.queryConfigurationVariable,

        routes.javascript.ContactController.updateContactForm,
        routes.javascript.ContactController.contact,

        routes.javascript.FileController.checkAccountKYCFileExists,
        routes.javascript.FileController.uploadAccountFileForm,
        routes.javascript.FileController.uploadAccountFile,
        routes.javascript.FileController.storeAccountFile,
        routes.javascript.FileController.updateAccountFileForm,
        routes.javascript.FileController.updateAccountFile,
        routes.javascript.FileController.uploadAccountKYCForm,
        routes.javascript.FileController.uploadAccountKYC,
        routes.javascript.FileController.storeAccountKYC,
        routes.javascript.FileController.updateAccountKYCForm,
        routes.javascript.FileController.updateAccountKYC,
        routes.javascript.FileController.file,

        routes.javascript.IssueAssetController.viewPendingIssueAssetRequests,
        routes.javascript.IssueAssetController.issueAssetForm,
        routes.javascript.IssueAssetController.rejectIssueAssetRequestForm,
        routes.javascript.IssueAssetController.issueAssetRequestForm,
        routes.javascript.IssueAssetController.blockchainIssueAssetForm,

        routes.javascript.IssueFiatController.viewPendingIssueFiatRequests,
        routes.javascript.IssueFiatController.issueFiatForm,
        routes.javascript.IssueFiatController.issueFiatRequestForm,
        routes.javascript.IssueFiatController.rejectIssueFiatRequestForm,
        routes.javascript.IssueFiatController.blockchainIssueFiatForm,
        routes.javascript.IssueFiatController.rejectIssueFiatRequestForm,

        routes.javascript.NotificationController.notificationPage,
        routes.javascript.NotificationController.unreadNotificationCount,
        routes.javascript.NotificationController.markNotificationRead,

        routes.javascript.RedeemAssetController.redeemAssetForm,
        routes.javascript.RedeemAssetController.blockchainRedeemAssetForm,

        routes.javascript.RedeemFiatController.redeemFiatForm,
        routes.javascript.RedeemFiatController.blockchainRedeemFiatForm,

        routes.javascript.ReleaseAssetController.releaseAssetList,
        routes.javascript.ReleaseAssetController.releaseAssetForm,
        routes.javascript.ReleaseAssetController.blockchainReleaseAssetForm,

        routes.javascript.SellerExecuteOrderController.moderatedSellerExecuteOrderList,
        routes.javascript.SellerExecuteOrderController.sellerExecuteOrderForm,
        routes.javascript.SellerExecuteOrderController.sellerExecuteOrderForm,
        routes.javascript.SellerExecuteOrderController.moderatedSellerExecuteOrderForm,
        routes.javascript.SellerExecuteOrderController.blockchainSellerExecuteOrderForm,

        routes.javascript.SendAssetController.sendAssetForm,
        routes.javascript.SendAssetController.blockchainSendAssetForm,

        routes.javascript.SendCoinController.requestCoinsForm,
        routes.javascript.SendCoinController.approveFaucetRequestsForm,
        routes.javascript.SendCoinController.rejectFaucetRequestForm,
        routes.javascript.SendCoinController.viewPendingFaucetRequests,
        routes.javascript.SendCoinController.sendCoinForm,
        routes.javascript.SendCoinController.blockchainSendCoinForm,

        routes.javascript.SendFiatController.sendFiatForm,
        routes.javascript.SendFiatController.blockchainSendFiatForm,

        routes.javascript.SetACLController.traderInvitationForm,
        routes.javascript.SetACLController.addTraderForm,
        routes.javascript.SetACLController.userUploadOrUpdateTraderKYCView,
        routes.javascript.SetACLController.userUploadTraderKYCForm,
        routes.javascript.SetACLController.userUploadTraderKYC,
        routes.javascript.SetACLController.userStoreTraderKYC,
        routes.javascript.SetACLController.userUpdateTraderKYCForm,
        routes.javascript.SetACLController.userUpdateTraderKYC,
        routes.javascript.SetACLController.reviewTraderCompletionForm,
        routes.javascript.SetACLController.viewTradersInOrganization,
        routes.javascript.SetACLController.viewTradersInOrganizationForZone,
        routes.javascript.SetACLController.viewTradersInOrganizationForGenesis,
        routes.javascript.SetACLController.zoneVerifyTraderForm,
        routes.javascript.SetACLController.organizationVerifyTraderForm,
        routes.javascript.SetACLController.organizationModifyTraderForm,
        routes.javascript.SetACLController.zoneRejectVerifyTraderRequestForm,
        routes.javascript.SetACLController.zoneViewPendingVerifyTraderRequests,
        routes.javascript.SetACLController.organizationRejectVerifyTraderRequestForm,
        routes.javascript.SetACLController.organizationViewPendingVerifyTraderRequests,
        routes.javascript.SetACLController.blockchainSetACLForm,
        routes.javascript.SetACLController.zoneViewKYCDocuments,
        routes.javascript.SetACLController.zoneVerifyKYCDocument,
        routes.javascript.SetACLController.zoneRejectKYCDocument,
        routes.javascript.SetACLController.organizationViewKYCDocuments,
        routes.javascript.SetACLController.organizationVerifyKYCDocument,
        routes.javascript.SetACLController.organizationRejectKYCDocument,
        routes.javascript.SetACLController.uploadTraderKYCForm,
        routes.javascript.SetACLController.uploadTraderKYC,
        routes.javascript.SetACLController.storeTraderKYC,
        routes.javascript.SetACLController.updateTraderKYCForm,
        routes.javascript.SetACLController.updateTraderKYC,
        routes.javascript.SetACLController.zoneViewKYCDocuments,
        routes.javascript.SetACLController.zoneVerifyKYCDocument,
        routes.javascript.SetACLController.zoneRejectKYCDocument,
        routes.javascript.SetACLController.organizationViewKYCDocuments,
        routes.javascript.SetACLController.organizationVerifyKYCDocument,
        routes.javascript.SetACLController.organizationRejectKYCDocument,

        routes.javascript.SetBuyerFeedbackController.setBuyerFeedbackForm,
        routes.javascript.SetBuyerFeedbackController.buyerFeedbackList,
        routes.javascript.SetBuyerFeedbackController.blockchainSetBuyerFeedbackForm,

        routes.javascript.SetSellerFeedbackController.setSellerFeedbackForm,
        routes.javascript.SetSellerFeedbackController.sellerFeedbackList,
        routes.javascript.SetSellerFeedbackController.blockchainSetSellerFeedbackForm,

        routes.javascript.VerifyEmailAddressController.verifyEmailAddressForm,

        routes.javascript.VerifyMobileNumberController.verifyMobileNumberForm,

        routes.javascript.ViewController.profile,
      )
    ).as("text/javascript")
  }
}
