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
        routes.javascript.AccountController.noteAndVerifyMnemonic,
        routes.javascript.AccountController.addIdentificationForm,
        routes.javascript.AccountController.userViewUploadOrUpdateIdentification,
        routes.javascript.AccountController.userReviewIdentificationForm,

        routes.javascript.AddKeyController.blockchainAddKeyForm,

        routes.javascript.AddOrganizationController.addOrganizationForm,
        routes.javascript.AddOrganizationController.userAddOrUpdateUBOsForm,
        routes.javascript.AddOrganizationController.userDeleteUBOForm,
        routes.javascript.AddOrganizationController.userAddUBOForm,
        routes.javascript.AddOrganizationController.addUBOForm,
        routes.javascript.AddOrganizationController.deleteUBOForm,
        routes.javascript.AddOrganizationController.addOrUpdateOrganizationBankAccountForm,
        routes.javascript.AddOrganizationController.userUploadOrUpdateOrganizationKYCView,
        routes.javascript.AddOrganizationController.userUploadOrganizationKYCForm,
        routes.javascript.AddOrganizationController.userUploadOrganizationKYC,
        routes.javascript.AddOrganizationController.userStoreOrganizationKYC,
        routes.javascript.AddOrganizationController.userUpdateOrganizationKYCForm,
        routes.javascript.AddOrganizationController.userUpdateOrganizationKYC,
        routes.javascript.AddOrganizationController.userReviewAddOrganizationRequestForm,
        routes.javascript.AddOrganizationController.updateOrganizationKYCDocumentStatusForm,
        routes.javascript.AddOrganizationController.acceptRequestForm,
        routes.javascript.AddOrganizationController.rejectRequestForm,
        routes.javascript.AddOrganizationController.blockchainAddOrganizationForm,
        routes.javascript.AddOrganizationController.uploadOrganizationKYCForm,
        routes.javascript.AddOrganizationController.uploadOrganizationKYC,
        routes.javascript.AddOrganizationController.storeOrganizationKYC,
        routes.javascript.AddOrganizationController.updateOrganizationKYCForm,
        routes.javascript.AddOrganizationController.updateOrganizationKYC,

        routes.javascript.AddZoneController.inviteZoneForm,
        routes.javascript.AddZoneController.viewPendingVerifyZoneRequests,
        routes.javascript.AddZoneController.addZoneForm,
        routes.javascript.AddZoneController.userUploadOrUpdateZoneKYCView,
        routes.javascript.AddZoneController.userReviewAddZoneRequestForm,
        routes.javascript.AddZoneController.verifyZoneForm,
        routes.javascript.AddZoneController.rejectVerifyZoneRequestForm,
        routes.javascript.AddZoneController.blockchainAddZoneForm,
        routes.javascript.AddZoneController.viewKYCDocuments,
        routes.javascript.AddZoneController.updateZoneKYCDocumentStatusForm,
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

        routes.javascript.AssetController.issueForm,
        routes.javascript.AssetController.billOfLadingContentForm,
        routes.javascript.AssetController.releaseForm,

        routes.javascript.BlockExplorerController.lastBlockHeight,
        routes.javascript.BlockExplorerController.blockDetails,
        routes.javascript.BlockExplorerController.stakingValidators,
        routes.javascript.BlockExplorerController.transactionHash,

        routes.javascript.ChangeBuyerBidController.blockchainChangeBuyerBidForm,

        routes.javascript.ChangeSellerBidController.blockchainChangeSellerBidForm,

        routes.javascript.ChatController.chatRoom,
        routes.javascript.ChatController.chatWindow,
        routes.javascript.ChatController.sendMessageForm,
        routes.javascript.ChatController.loadMoreChats,
        routes.javascript.ChatController.replyToMessage,
        routes.javascript.ChatController.markChatAsRead,

        routes.javascript.ComponentViewController.commonHome,
        routes.javascript.ComponentViewController.fiatList,
        routes.javascript.ComponentViewController.traderViewAcceptedNegotiationList,
        routes.javascript.ComponentViewController.traderViewAcceptedBuyNegotiationList,
        routes.javascript.ComponentViewController.traderViewAcceptedSellNegotiationList,
        routes.javascript.ComponentViewController.traderViewSentReceivedIncompleteRejectedFailedNegotiationList,
        routes.javascript.ComponentViewController.traderViewSentNegotiationRequestList,
        routes.javascript.ComponentViewController.traderViewReceivedNegotiationRequestList,
        routes.javascript.ComponentViewController.traderViewIncompleteNegotiationList,
        routes.javascript.ComponentViewController.traderViewRejectedAndFailedNegotiationList,

        routes.javascript.ComponentViewController.organizationViewTraderAccountList,
        routes.javascript.ComponentViewController.organizationViewAcceptedTraderAccountList,
        routes.javascript.ComponentViewController.organizationViewAcceptedTraderAccount,
        routes.javascript.ComponentViewController.organizationViewPendingTraderRequestList,
        routes.javascript.ComponentViewController.organizationViewPendingTraderRequest,
        routes.javascript.ComponentViewController.organizationViewRejectedTraderRequestList,
        routes.javascript.ComponentViewController.organizationViewRejectedTraderRequest,

        routes.javascript.ComponentViewController.zoneViewTraderAccountList,
        routes.javascript.ComponentViewController.zoneViewAcceptedTraderAccountList,
        routes.javascript.ComponentViewController.zoneViewAcceptedTraderAccount,
        routes.javascript.ComponentViewController.zoneViewPendingTraderRequestList,
        routes.javascript.ComponentViewController.zoneViewPendingTraderRequest,
        routes.javascript.ComponentViewController.zoneViewRejectedTraderRequestList,
        routes.javascript.ComponentViewController.zoneViewRejectedTraderRequest,

        routes.javascript.ComponentViewController.zoneViewOrganizationAccountList,
        routes.javascript.ComponentViewController.zoneViewAcceptedOrganizationAccountList,
        routes.javascript.ComponentViewController.zoneViewAcceptedOrganizationAccount,
        routes.javascript.ComponentViewController.zoneViewPendingOrganizationRequestList,
        routes.javascript.ComponentViewController.zoneViewPendingOrganizationRequest,
        routes.javascript.ComponentViewController.zoneViewRejectedOrganizationRequestList,
        routes.javascript.ComponentViewController.zoneViewRejectedOrganizationRequest,

        routes.javascript.ComponentViewController.zoneViewAcceptedNegotiationList,
        routes.javascript.ComponentViewController.zoneViewAcceptedNegotiation,
        routes.javascript.ComponentViewController.zoneTradeStatistics,
        routes.javascript.ComponentViewController.zoneViewTradeRoomFinancialAndChecks,
        routes.javascript.ComponentViewController.zoneViewTradeRoomFinancial,
        routes.javascript.ComponentViewController.zoneViewTradeRoomChecks,
        routes.javascript.ComponentViewController.traderViewTradeRoomFinancialAndChecks,
        routes.javascript.ComponentViewController.traderViewTradeRoomFinancial,
        routes.javascript.ComponentViewController.traderViewTradeRoomChecks,
        routes.javascript.ComponentViewController.organizationViewTradeRoomFinancialAndChecks,
        routes.javascript.ComponentViewController.organizationViewTradeRoomFinancial,
        routes.javascript.ComponentViewController.organizationViewTradeRoomChecks,
        routes.javascript.ComponentViewController.zoneOrderActions,

        routes.javascript.ComponentViewController.organizationSubscription,
        routes.javascript.ComponentViewController.traderSubscription,

        routes.javascript.ComponentViewController.organizationTradeStatistics,
        routes.javascript.ComponentViewController.traderTradeStatistics,

        routes.javascript.ComponentViewController.traderViewRejectedAndFailedNegotiationList,
        routes.javascript.ComponentViewController.organizationViewAcceptedNegotiationList,
        routes.javascript.ComponentViewController.organizationViewAcceptedBuyNegotiationList,
        routes.javascript.ComponentViewController.organizationViewAcceptedSellNegotiationList,
        routes.javascript.ComponentViewController.organizationViewSentReceivedIncompleteRejectedFailedNegotiationList,
        routes.javascript.ComponentViewController.organizationViewSentNegotiationRequestList,
        routes.javascript.ComponentViewController.organizationViewReceivedNegotiationList,
        routes.javascript.ComponentViewController.organizationViewIncompleteNegotiationList,
        routes.javascript.ComponentViewController.organizationViewRejectedAndFailedNegotiationList,
        routes.javascript.ComponentViewController.profilePicture,
        routes.javascript.ComponentViewController.identification,
        routes.javascript.ComponentViewController.userViewPendingRequests,
        routes.javascript.ComponentViewController.traderViewOrganization,
        routes.javascript.ComponentViewController.organization,
        routes.javascript.ComponentViewController.traderRelationList,
        routes.javascript.ComponentViewController.acceptedTraderRelationList,
        routes.javascript.ComponentViewController.pendingTraderRelationList,
        routes.javascript.ComponentViewController.acceptedTraderRelation,
        routes.javascript.ComponentViewController.pendingSentTraderRelation,
        routes.javascript.ComponentViewController.pendingReceivedTraderRelation,
        routes.javascript.ComponentViewController.organizationBankAccount,
        routes.javascript.ComponentViewController.traderViewOrganizationBankAccount,
        routes.javascript.ComponentViewController.zoneViewOrganizationBankAccount,
        routes.javascript.ComponentViewController.userViewOrganizationUBOs,
        routes.javascript.ComponentViewController.viewOrganizationUBOs,
        routes.javascript.ComponentViewController.recentActivities,
        routes.javascript.ComponentViewController.traderFinancials,
        routes.javascript.ComponentViewController.traderViewAcceptedNegotiation,
        routes.javascript.ComponentViewController.traderViewAcceptedNegotiationTerms,
        routes.javascript.ComponentViewController.organizationViewAcceptedNegotiation,
        routes.javascript.ComponentViewController.organizationViewAcceptedNegotiationTerms,
        routes.javascript.ComponentViewController.zoneViewAcceptedNegotiation,
        routes.javascript.ComponentViewController.zoneViewAcceptedNegotiationTerms,
        routes.javascript.ComponentViewController.organizationDeclarations,
        routes.javascript.ComponentViewController.traderViewNegotiationDocumentList,
        routes.javascript.ComponentViewController.traderViewNegotiationDocument,
        routes.javascript.ComponentViewController.traderViewAcceptedNegotiationDocumentList,
        routes.javascript.ComponentViewController.organizationViewNegotiationDocumentList,
        routes.javascript.ComponentViewController.organizationViewNegotiationDocument,
        routes.javascript.ComponentViewController.organizationViewAcceptedNegotiationDocumentList,
        routes.javascript.ComponentViewController.zoneViewNegotiationDocumentList,
        routes.javascript.ComponentViewController.zoneViewNegotiationDocument,
        routes.javascript.ComponentViewController.zoneViewAcceptedNegotiationDocumentList,
        routes.javascript.ComponentViewController.tradeDocuments,
        routes.javascript.ComponentViewController.tradeActivities,
        routes.javascript.ComponentViewController.traderViewNegotiationDocumentContent,
        routes.javascript.ComponentViewController.traderViewAssetDocumentContent,
        routes.javascript.ComponentViewController.organizationViewNegotiationDocumentContent,
        routes.javascript.ComponentViewController.organizationViewAssetDocumentContent,
        routes.javascript.ComponentViewController.zoneViewNegotiationDocumentContent,
        routes.javascript.ComponentViewController.zoneViewAssetDocumentContent,
        routes.javascript.ConfirmBuyerBidController.blockchainConfirmBuyerBidForm,

        routes.javascript.ConfirmSellerBidController.blockchainConfirmSellerBidForm,

        routes.javascript.ConfigurationController.queryConfigurationVariable,

        routes.javascript.ContactController.addOrUpdateEmailAddressForm,
        routes.javascript.ContactController.addOrUpdateMobileNumberForm,
        routes.javascript.ContactController.contact,

        routes.javascript.FileController.uploadAccountFileForm,
        routes.javascript.FileController.uploadAccountFile,
        routes.javascript.FileController.storeAccountFile,
        routes.javascript.FileController.updateAccountFileForm,
        routes.javascript.FileController.updateAccountFile,
        routes.javascript.FileController.genesisAccessedFile,
        routes.javascript.FileController.zoneAccessedOrganizationKYCFile,
        routes.javascript.FileController.uploadAssetForm,
        routes.javascript.FileController.uploadAsset,
        routes.javascript.FileController.storeAsset,
        routes.javascript.FileController.updateAssetForm,
        routes.javascript.FileController.updateAsset,
        routes.javascript.FileController.uploadNegotiationForm,
        routes.javascript.FileController.uploadNegotiation,
        routes.javascript.FileController.storeNegotiation,
        routes.javascript.FileController.updateNegotiationForm,
        routes.javascript.FileController.updateNegotiation,
        routes.javascript.FileController.file,
        routes.javascript.FileController.uploadAccountKYCForm,
        routes.javascript.FileController.uploadAccountKYC,
        routes.javascript.FileController.storeAccountKYC,
        routes.javascript.FileController.updateAccountKYCForm,
        routes.javascript.FileController.updateAccountKYC,


        routes.javascript.IssueAssetController.viewPendingIssueAssetRequests,
        routes.javascript.IssueAssetController.issueAssetForm,
        routes.javascript.IssueAssetController.blockchainIssueAssetForm,
        routes.javascript.IssueAssetController.viewPendingIssueAssetRequests,
        routes.javascript.IssueAssetController.issueAssetForm,

        routes.javascript.IssueFiatController.issueFiatForm,
        routes.javascript.IssueFiatController.issueFiatRequestForm,
        routes.javascript.IssueFiatController.blockchainIssueFiatForm,

        routes.javascript.NegotiationController.requestForm,
        routes.javascript.NegotiationController.paymentTermsForm,
        routes.javascript.NegotiationController.documentListForm,
        routes.javascript.NegotiationController.reviewRequestForm,
        routes.javascript.NegotiationController.acceptRequestForm,
        routes.javascript.NegotiationController.rejectRequestForm,
        routes.javascript.NegotiationController.updateAssetTermsForm,
        routes.javascript.NegotiationController.updateAssetOtherDetailsForm,
        routes.javascript.NegotiationController.updatePaymentTermsForm,
        routes.javascript.NegotiationController.updateDocumentListForm,
        routes.javascript.NegotiationController.acceptOrRejectNegotiationTermsForm,
        routes.javascript.NegotiationController.invoiceContentForm,
        routes.javascript.NegotiationController.contractContentForm,
        routes.javascript.NegotiationController.confirmAllNegotiationTermsForm,
        routes.javascript.NegotiationController.buyerConfirmForm,
        routes.javascript.NegotiationController.sellerConfirmForm,

        routes.javascript.NegotiationController.tradeActivityMessages,

        routes.javascript.NotificationController.recentActivityMessages,
        routes.javascript.NotificationController.unreadNotificationCount,
        routes.javascript.NotificationController.markNotificationRead,

        routes.javascript.OrderController.moderatedBuyerExecuteForm,
        routes.javascript.OrderController.moderatedSellerExecuteForm,
        routes.javascript.OrderController.blockchainBuyerExecute,
        routes.javascript.OrderController.blockchainSellerExecute,

        routes.javascript.RedeemAssetController.redeemAssetForm,
        routes.javascript.RedeemAssetController.blockchainRedeemAssetForm,

        routes.javascript.RedeemFiatController.redeemFiatForm,
        routes.javascript.RedeemFiatController.blockchainRedeemFiatForm,

        routes.javascript.ReleaseAssetController.blockchainReleaseAssetForm,

        routes.javascript.SendAssetController.sendAssetForm,
        routes.javascript.SendAssetController.blockchainSendAssetForm,

        routes.javascript.SendCoinController.faucetRequestForm,
        routes.javascript.SendCoinController.approveFaucetRequestsForm,
        routes.javascript.SendCoinController.rejectFaucetRequestForm,
        routes.javascript.SendCoinController.faucetRequestList,
        routes.javascript.SendCoinController.sendCoinForm,
        routes.javascript.SendCoinController.blockchainSendCoinForm,

        routes.javascript.SendFiatController.sendFiatForm,
        routes.javascript.SendFiatController.blockchainSendFiatForm,

        routes.javascript.SetACLController.inviteTraderForm,
        routes.javascript.SetACLController.addTraderForm,
        routes.javascript.SetACLController.zoneVerifyTraderForm,
        routes.javascript.SetACLController.organizationVerifyTraderForm,
        routes.javascript.SetACLController.blockchainSetACLForm,

        routes.javascript.SetBuyerFeedbackController.setBuyerFeedbackForm,
        routes.javascript.SetBuyerFeedbackController.buyerFeedbackList,
        routes.javascript.SetBuyerFeedbackController.blockchainSetBuyerFeedbackForm,

        routes.javascript.SetSellerFeedbackController.setSellerFeedbackForm,
        routes.javascript.SetSellerFeedbackController.sellerFeedbackList,
        routes.javascript.SetSellerFeedbackController.blockchainSetSellerFeedbackForm,

        routes.javascript.TraderController.traderRelationRequestForm,
        routes.javascript.TraderController.organizationRejectRequestForm,
        routes.javascript.TraderController.zoneRejectRequestForm,
        routes.javascript.TraderController.organizationModifyTraderForm,

        routes.javascript.ContactController.verifyEmailAddressForm,

        routes.javascript.ContactController.verifyMobileNumberForm,

        routes.javascript.ViewController.profile,
        routes.javascript.ViewController.account,
        routes.javascript.ViewController.dashboard,
        routes.javascript.ViewController.trades,
        routes.javascript.ViewController.tradeRoom,

        routes.javascript.BackgroundCheckController.uploadTraderBackgroundCheckFileForm,
        routes.javascript.BackgroundCheckController.uploadTraderBackgroundCheckFile,
        routes.javascript.BackgroundCheckController.storeTraderBackgroundCheckFile,
        routes.javascript.BackgroundCheckController.updateTraderBackgroundCheckFileForm,
        routes.javascript.BackgroundCheckController.updateTraderBackgroundCheckFile,
        routes.javascript.BackgroundCheckController.uploadOrUpdateTraderBackgroundCheckFile,
        routes.javascript.BackgroundCheckController.uploadOrganizationBackgroundCheckFileForm,
        routes.javascript.BackgroundCheckController.uploadOrganizationBackgroundCheckFile,
        routes.javascript.BackgroundCheckController.storeOrganizationBackgroundCheckFile,
        routes.javascript.BackgroundCheckController.updateOrganizationBackgroundCheckFileForm,
        routes.javascript.BackgroundCheckController.updateOrganizationBackgroundCheckFile,
        routes.javascript.BackgroundCheckController.uploadOrUpdateOrganizationBackgroundCheckFile,

      )
    ).as("text/javascript")
  }
}
