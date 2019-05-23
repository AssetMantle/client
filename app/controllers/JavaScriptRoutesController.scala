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
        routes.javascript.SetSellerFeedbackController.blockchainSetSellerFeedbackForm

      )
    ).as("text/javascript")
  }
}
