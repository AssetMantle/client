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
        routes.javascript.SignUpController.checkUsernameAvailable,
        routes.javascript.ConfigurationController.queryConfigurationVariable,
        routes.javascript.NotificationController.notificationPage,
        routes.javascript.NotificationController.unreadNotificationCount,
        routes.javascript.NotificationController.markNotificationRead,
        routes.javascript.LoginController.loginForm,
        routes.javascript.LogoutController.logoutForm,
        routes.javascript.SignUpController.signUpForm,
        routes.javascript.UpdateContactController.updateContactForm,
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
        routes.javascript.IssueAssetController.rejectIssueAssetRequestForm,
        routes.javascript.IssueAssetController.issueAssetRequestForm,
        routes.javascript.IssueFiatController.viewPendingIssueFiatRequests,
        routes.javascript.IssueFiatController.issueFiatForm,
        routes.javascript.IssueFiatController.issueFiatRequestForm,
        routes.javascript.IssueFiatController.rejectIssueFiatRequestForm,
        routes.javascript.RedeemAssetController.redeemAssetForm,
        routes.javascript.RedeemFiatController.redeemFiatForm,
        routes.javascript.ReleaseAssetController.releaseAssetForm,
        routes.javascript.SellerExecuteOrderController.sellerExecuteOrderForm,
        routes.javascript.SellerExecuteOrderController.moderatedSellerExecuteOrderForm,
        routes.javascript.SendAssetController.sendAssetForm,
        routes.javascript.SendCoinController.requestCoinsForm,
        routes.javascript.SendCoinController.approveFaucetRequestsForm,
        routes.javascript.SendCoinController.rejectFaucetRequestForm,
        routes.javascript.SendCoinController.viewPendingFaucetRequests,
        routes.javascript.SendCoinController.sendCoinForm,
        routes.javascript.SendFiatController.sendFiatForm,
        routes.javascript.SetACLController.setACLForm,
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

        routes.javascript.FileController.uploadAccountFileForm,
        routes.javascript.FileController.uploadAccountFile,
        routes.javascript.FileController.storeAccountFile,
        routes.javascript.FileController.updateAccountFileForm,
        routes.javascript.FileController.updateAccountFile,

        routes.javascript.FileController.uploadUserKycForm,
        routes.javascript.FileController.uploadUserKYC,
        routes.javascript.FileController.storeUserKYC,
        routes.javascript.FileController.updateUserKycForm,
        routes.javascript.FileController.updateUserKYC,

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

        routes.javascript.FileController.uploadZoneKycForm,
        routes.javascript.FileController.uploadZoneKYC,
        routes.javascript.FileController.storeZoneKYC,
        routes.javascript.FileController.updateZoneKycForm,
        routes.javascript.FileController.updateZoneKYC,

        routes.javascript.FileController.uploadOrganizationKycForm,
        routes.javascript.FileController.uploadOrganizationKYC,
        routes.javascript.FileController.storeOrganizationKYC,
        routes.javascript.FileController.updateOrganizationKycForm,
        routes.javascript.FileController.updateOrganizationKYC,

        routes.javascript.FileController.genesisAccessedFile,
        routes.javascript.FileController.zoneAccessedFile,
        routes.javascript.FileController.file,

        routes.javascript.AddZoneController.viewKycDocuments,
        routes.javascript.AddZoneController.verifyKycDocument,
        routes.javascript.AddZoneController.rejectKycDocument,
        routes.javascript.AddOrganizationController.viewKycDocuments,
        routes.javascript.AddOrganizationController.verifyKycDocument,
        routes.javascript.AddOrganizationController.rejectKycDocument,
      )
    ).as("text/javascript")
  }
}
