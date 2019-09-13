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
        routes.javascript.AccountController.checkUsernameAvailable,
        routes.javascript.ConfigurationController.queryConfigurationVariable,
        routes.javascript.NotificationController.notificationPage,
        routes.javascript.NotificationController.unreadNotificationCount,
        routes.javascript.NotificationController.markNotificationRead,
        routes.javascript.LoginController.loginForm,
        routes.javascript.LogoutController.logoutForm,
        routes.javascript.AccountController.signUpForm,
        routes.javascript.ContactController.updateContactForm,
        routes.javascript.VerifyMobileNumberController.verifyMobileNumberForm,
        routes.javascript.VerifyEmailAddressController.verifyEmailAddressForm,
        routes.javascript.AddOrganizationController.addOrganizationForm,
        routes.javascript.AddOrganizationController.viewPendingVerifyOrganizationRequests,
        routes.javascript.AddOrganizationController.verifyOrganizationForm,
        routes.javascript.AddOrganizationController.rejectVerifyOrganizationRequestForm,
        routes.javascript.AddZoneController.viewPendingVerifyZoneRequests,
        routes.javascript.AddZoneController.addZoneForm,
        routes.javascript.AddZoneController.verifyZoneForm,
        routes.javascript.AddZoneController.rejectVerifyZoneRequestForm,
        routes.javascript.BuyerExecuteOrderController.buyerExecuteOrderForm,
        routes.javascript.BuyerExecuteOrderController.moderatedBuyerExecuteOrderForm,
        routes.javascript.ChangeBuyerBidController.changeBuyerBidForm,
        routes.javascript.ChangeSellerBidController.changeSellerBidForm,
        routes.javascript.ConfirmBuyerBidController.confirmBuyerBidForm,
        routes.javascript.ConfirmSellerBidController.confirmSellerBidForm,
        routes.javascript.IssueAssetController.viewPendingIssueAssetRequests,
        routes.javascript.IssueAssetController.issueAssetForm,
        routes.javascript.IssueAssetController.issueAssetRequestForm,
        routes.javascript.IssueAssetController.rejectIssueAssetRequestForm,
        routes.javascript.IssueAssetController.issueAssetDetailForm,
        routes.javascript.IssueAssetController.issueAssetOBLForm,
        routes.javascript.IssueAssetController.assetDetail,
        routes.javascript.IssueAssetController.issueAssetInvoiceForm,
        routes.javascript.IssueFiatController.viewPendingIssueFiatRequests,
        routes.javascript.IssueFiatController.issueFiatForm,
        routes.javascript.IssueFiatController.issueFiatRequestForm,
        routes.javascript.IssueFiatController.rejectIssueFiatRequestForm,

        routes.javascript.ComponentViewController.commonHome,
        routes.javascript.ComponentViewController.genesisDetails,
        routes.javascript.ComponentViewController.zoneDetails,
        routes.javascript.ComponentViewController.zoneDetails,
        routes.javascript.ComponentViewController.organizationDetails,
        routes.javascript.ComponentViewController.assetList,
        routes.javascript.ComponentViewController.fiatList,
        routes.javascript.ComponentViewController.buyNegotiationList,
        routes.javascript.ComponentViewController.sellNegotiationList,
        routes.javascript.ComponentViewController.orderList,
        routes.javascript.ComponentViewController.availableAssetList,
        routes.javascript.ComponentViewController.availableAssetListWithLogin,
        routes.javascript.ComponentViewController.profileDocuments,
        routes.javascript.ComponentViewController.profilePicture,

        routes.javascript.ViewController.profile,

        routes.javascript.ReleaseAssetController.releaseAssetList,
        routes.javascript.BuyerExecuteOrderController.moderatedBuyerExecuteOrderList,
        routes.javascript.SellerExecuteOrderController.moderatedSellerExecuteOrderList,
        routes.javascript.SetBuyerFeedbackController.buyerFeedbackList,
        routes.javascript.SetSellerFeedbackController.sellerFeedbackList,
        routes.javascript.SetACLController.viewTradersInOrganization,
        routes.javascript.SetACLController.viewTradersInOrganizationForZone,
        routes.javascript.SetACLController.viewTradersInOrganizationForGenesis,
        routes.javascript.AddOrganizationController.viewOrganizationsInZone,
        routes.javascript.AddOrganizationController.viewOrganizationsInZoneForGenesis,
        routes.javascript.AddZoneController.viewZonesInGenesis,

        routes.javascript.RedeemAssetController.redeemAssetForm,
        routes.javascript.RedeemFiatController.redeemFiatForm,
        routes.javascript.ReleaseAssetController.releaseAssetForm,
        routes.javascript.SellerExecuteOrderController.sellerExecuteOrderForm,
        routes.javascript.SellerExecuteOrderController.sellerExecuteOrderForm,
        routes.javascript.SellerExecuteOrderController.moderatedSellerExecuteOrderForm,
        routes.javascript.SendAssetController.sendAssetForm,
        routes.javascript.SendCoinController.requestCoinsForm,
        routes.javascript.SendCoinController.approveFaucetRequestsForm,
        routes.javascript.SendCoinController.rejectFaucetRequestForm,
        routes.javascript.SendCoinController.viewPendingFaucetRequests,
        routes.javascript.SendCoinController.sendCoinForm,
        routes.javascript.SendFiatController.sendFiatForm,

        routes.javascript.SetACLController.zoneVerifyTraderForm,
        routes.javascript.SetACLController.organizationVerifyTraderForm,
        routes.javascript.SetACLController.addTraderForm,
        routes.javascript.SetACLController.zoneRejectVerifyTraderRequestForm,
        routes.javascript.SetACLController.zoneViewPendingVerifyTraderRequests,
        routes.javascript.SetACLController.organizationRejectVerifyTraderRequestForm,
        routes.javascript.SetACLController.organizationViewPendingVerifyTraderRequests,

        routes.javascript.SetBuyerFeedbackController.setBuyerFeedbackForm,
        routes.javascript.SetSellerFeedbackController.setSellerFeedbackForm,

        routes.javascript.AddKeyController.blockchainAddKeyForm,
        routes.javascript.AddOrganizationController.blockchainAddOrganizationForm,
        routes.javascript.AddZoneController.blockchainAddZoneForm,
        routes.javascript.BuyerExecuteOrderController.blockchainBuyerExecuteOrderForm,
        routes.javascript.ChangeBuyerBidController.blockchainChangeBuyerBidForm,
        routes.javascript.ChangeSellerBidController.blockchainChangeSellerBidForm,
        routes.javascript.ConfirmBuyerBidController.blockchainConfirmBuyerBidForm,
        routes.javascript.ConfirmSellerBidController.blockchainConfirmSellerBidForm,
        routes.javascript.IssueAssetController.blockchainIssueAssetForm,
        routes.javascript.IssueFiatController.blockchainIssueFiatForm,
        routes.javascript.IssueFiatController.rejectIssueFiatRequestForm,
        routes.javascript.RedeemAssetController.blockchainRedeemAssetForm,
        routes.javascript.RedeemFiatController.blockchainRedeemFiatForm,
        routes.javascript.ReleaseAssetController.blockchainReleaseAssetForm,
        routes.javascript.SellerExecuteOrderController.blockchainSellerExecuteOrderForm,
        routes.javascript.SendAssetController.blockchainSendAssetForm,
        routes.javascript.SendCoinController.blockchainSendCoinForm,
        routes.javascript.SendFiatController.blockchainSendFiatForm,
        routes.javascript.SetACLController.blockchainSetACLForm,
        routes.javascript.SetBuyerFeedbackController.blockchainSetBuyerFeedbackForm,
        routes.javascript.SetSellerFeedbackController.blockchainSetSellerFeedbackForm,

        routes.javascript.FileController.checkAccountKycFileExists,
        routes.javascript.FileController.checkZoneKycFileExists,
        routes.javascript.FileController.checkOrganizationKycFileExists,
        routes.javascript.FileController.checkTraderKycFileExists,
        routes.javascript.FileController.checkTraderAssetFileExists,
        routes.javascript.FileController.checkTraderNegotiationFileExists,

        routes.javascript.FileController.uploadAccountFileForm,
        routes.javascript.FileController.uploadAccountFile,
        routes.javascript.FileController.storeAccountFile,
        routes.javascript.FileController.updateAccountFileForm,
        routes.javascript.FileController.updateAccountFile,

        routes.javascript.FileController.uploadUserKycForm,
        routes.javascript.FileController.uploadUserKyc,
        routes.javascript.FileController.storeUserKyc,
        routes.javascript.FileController.updateUserKycForm,
        routes.javascript.FileController.updateUserKyc,

        routes.javascript.FileController.uploadUserZoneKycForm,
        routes.javascript.FileController.uploadUserZoneKyc,
        routes.javascript.FileController.storeUserZoneKyc,
        routes.javascript.FileController.updateUserZoneKycForm,
        routes.javascript.FileController.updateUserZoneKyc,

        routes.javascript.FileController.uploadUserOrganizationKycForm,
        routes.javascript.FileController.uploadUserOrganizationKyc,
        routes.javascript.FileController.storeUserOrganizationKyc,
        routes.javascript.FileController.updateUserOrganizationKycForm,
        routes.javascript.FileController.updateUserOrganizationKyc,

        routes.javascript.FileController.uploadUserTraderKycForm,
        routes.javascript.FileController.uploadUserTraderKyc,
        routes.javascript.FileController.storeUserTraderKyc,
        routes.javascript.FileController.updateUserTraderKycForm,
        routes.javascript.FileController.updateUserTraderKyc,

        routes.javascript.FileController.uploadZoneKycForm,
        routes.javascript.FileController.uploadZoneKyc,
        routes.javascript.FileController.storeZoneKyc,
        routes.javascript.FileController.updateZoneKycForm,
        routes.javascript.FileController.updateZoneKyc,

        routes.javascript.FileController.uploadOrganizationKycForm,
        routes.javascript.FileController.uploadOrganizationKyc,
        routes.javascript.FileController.storeOrganizationKyc,
        routes.javascript.FileController.updateOrganizationKycForm,
        routes.javascript.FileController.updateOrganizationKyc,

        routes.javascript.FileController.uploadTraderKycForm,
        routes.javascript.FileController.uploadTraderKyc,
        routes.javascript.FileController.storeTraderKyc,
        routes.javascript.FileController.updateTraderKycForm,
        routes.javascript.FileController.updateTraderKyc,

        routes.javascript.FileController.uploadTraderAssetForm,
        routes.javascript.FileController.uploadTraderAsset,
        routes.javascript.FileController.storeTraderAsset,
        routes.javascript.FileController.updateTraderAssetForm,
        routes.javascript.FileController.updateTraderAsset,

        routes.javascript.FileController.uploadTraderNegotiationForm,
        routes.javascript.FileController.uploadTraderNegotiation,
        routes.javascript.FileController.storeTraderNegotiation,
        routes.javascript.FileController.updateTraderNegotiationForm,
        routes.javascript.FileController.updateTraderNegotiation,

        routes.javascript.FileController.genesisAccessedFile,
        routes.javascript.FileController.zoneAccessedOrganizationFile,
        routes.javascript.FileController.zoneAccessedTraderFile,
        routes.javascript.FileController.file,

        routes.javascript.AddZoneController.viewKycDocuments,
        routes.javascript.AddZoneController.verifyKycDocument,
        routes.javascript.AddZoneController.rejectKycDocument,
        routes.javascript.AddOrganizationController.viewKycDocuments,
        routes.javascript.AddOrganizationController.verifyKycDocument,
        routes.javascript.AddOrganizationController.rejectKycDocument,
        routes.javascript.IssueAssetController.viewAssetDocuments,
        routes.javascript.IssueAssetController.verifyAssetDocument,
        routes.javascript.IssueAssetController.rejectAssetDocument,
        routes.javascript.SetACLController.zoneViewKycDocuments,
        routes.javascript.SetACLController.zoneVerifyKycDocument,
        routes.javascript.SetACLController.zoneRejectKycDocument,
        routes.javascript.SetACLController.organizationViewKycDocuments,
        routes.javascript.SetACLController.organizationVerifyKycDocument,
        routes.javascript.SetACLController.organizationRejectKycDocument,

        //profile
        routes.javascript.ContactController.contact,

        routes.javascript.AccountController.changePasswordForm,
        routes.javascript.AccountController.emailOTPForgotPasswordForm,
        routes.javascript.AccountController.forgotPasswordForm,
      )
    ).as("text/javascript")
  }
}
