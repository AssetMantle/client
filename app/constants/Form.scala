package constants

import controllers.routes
import play.api.mvc.Call
import play.api.routing.JavaScriptReverseRoute

class Form(template: String, val route: Call, val get: JavaScriptReverseRoute) {
  val legend: String = Seq("FORM", template, "LEGEND").mkString(".")
  val submit: String = Seq("FORM", template, "SUBMIT").mkString(".")
  val button: String = Seq("FORM", template, "BUTTON").mkString(".")
}

object Form {

  //AccountController
  val SIGN_UP = new Form("SIGN_UP", routes.AccountController.signUp(), routes.javascript.AccountController.signUpForm)
  val CREATE_WALLET = new Form("CREATE_WALLET", routes.AccountController.createWallet(), routes.javascript.AccountController.createWalletForm)
  val LOGIN = new Form("LOGIN", routes.AccountController.login(), routes.javascript.AccountController.loginForm)
  val LOGOUT = new Form("LOGOUT", routes.AccountController.logout(), routes.javascript.AccountController.logoutForm)
  val CHANGE_PASSWORD = new Form("CHANGE_PASSWORD", routes.AccountController.changePassword(), routes.javascript.AccountController.changePasswordForm)
  val EMAIL_OTP_FORGOT_PASSWORD = new Form("EMAIL_OTP_FORGOT_PASSWORD", routes.AccountController.emailOTPForgotPassword(), routes.javascript.AccountController.emailOTPForgotPasswordForm)
  val FORGOT_PASSWORD = new Form("FORGOT_PASSWORD", routes.AccountController.forgotPassword(), routes.javascript.AccountController.forgotPasswordForm)
  val ADD_IDENTIFICATION = new Form("ADD_IDENTIFICATION", routes.AccountController.addIdentification(), routes.javascript.AccountController.addIdentificationForm)
  val USER_REVIEW_IDENTIFICATION = new Form("USER_REVIEW_IDENTIFICATION", routes.AccountController.userReviewIdentification(), routes.javascript.AccountController.userReviewIdentificationForm)

  //AddKeyController
  val BLOCKCHAIN_ADD_KEY = new Form("BLOCKCHAIN_ADD_KEY", routes.AddKeyController.blockchainAddKey(), routes.javascript.AddKeyController.blockchainAddKeyForm)

  //AddOrganizationController
  val ADD_ORGANIZATION = new Form("ADD_ORGANIZATION", routes.AddOrganizationController.addOrganization(), routes.javascript.AddOrganizationController.addOrganizationForm)
  val DELETE_UBO = new Form("DELETE_UBO", routes.AddOrganizationController.deleteUBO(), routes.javascript.AddOrganizationController.deleteUBOForm)
  val USER_DELETE_UBO = new Form("USER_DELETE_UBO", routes.AddOrganizationController.userDeleteUBO(), routes.javascript.AddOrganizationController.userDeleteUBOForm)
  val ADD_UBO = new Form("ADD_UBO", routes.AddOrganizationController.addUBO(), routes.javascript.AddOrganizationController.addUBOForm)
  val USER_ADD_UBO = new Form("USER_ADD_UBO", routes.AddOrganizationController.userAddUBO(), routes.javascript.AddOrganizationController.userAddUBOForm)
  val ADD_OR_UPDATE_ORGANIZATION_BANK_ACCOUNT = new Form("ADD_OR_UPDATE_ORGANIZATION_BANK_ACCOUNT", routes.AddOrganizationController.addOrUpdateOrganizationBankAccount(), routes.javascript.AddOrganizationController.addOrUpdateOrganizationBankAccountForm)
  val USER_REVIEW_ADD_ORGANIZATION_REQUEST = new Form("USER_REVIEW_ADD_ORGANIZATION_REQUEST", routes.AddOrganizationController.userReviewAddOrganizationRequest(), routes.javascript.AddOrganizationController.userReviewAddOrganizationRequestForm)
  val ACCEPT_ORGANIZATION_REQUEST = new Form("ACCEPT_ORGANIZATION_REQUEST", routes.AddOrganizationController.acceptRequest(), routes.javascript.AddOrganizationController.acceptRequestForm)
  val REJECT_ORGANIZATION_REQUEST = new Form("REJECT_ORGANIZATION_REQUEST", routes.AddOrganizationController.rejectRequest(), routes.javascript.AddOrganizationController.rejectRequestForm)
  val BLOCKCHAIN_ADD_ORGANIZATION = new Form("BLOCKCHAIN_ADD_ORGANIZATION", routes.AddOrganizationController.blockchainAddOrganization(), routes.javascript.AddOrganizationController.blockchainAddOrganizationForm)
  val UPDATE_ORGANIZATION_KYC_DOCUMENT_STATUS = new Form("UPDATE_ORGANIZATION_KYC_DOCUMENT_STATUS", routes.AddOrganizationController.updateOrganizationKYCDocumentStatus(), routes.javascript.AddOrganizationController.updateOrganizationKYCDocumentStatusForm)

  //AssetController
  val ISSUE_ASSET = new Form("ISSUE_ASSET", routes.AssetController.issue(), routes.javascript.AssetController.issueForm)
  val ADD_BILL_OF_LADING = new Form("ADD_BILL_OF_LADING", routes.AssetController.addBillOfLading(), routes.javascript.AssetController.addBillOfLadingForm)
  val RELEASE_ASSET = new Form("RELEASE_ASSET", routes.AssetController.release(), routes.javascript.AssetController.releaseForm)
  val SEND_ASSET = new Form("SEND_ASSET", routes.AssetController.send(), routes.javascript.AssetController.sendForm)
  val REDEEM_ASSET = new Form("REDEEM_ASSET", routes.AssetController.redeem(), routes.javascript.AssetController.redeemForm)
  val ACCEPT_OR_REJECT_ASSET_DOCUMENT = new Form("ACCEPT_OR_REJECT_ASSET_DOCUMENT", routes.AssetController.acceptOrRejectAssetDocument(), routes.javascript.AssetController.acceptOrRejectAssetDocumentForm)

  //AddZoneController
  val INVITE_ZONE = new Form("INVITE_ZONE", routes.AddZoneController.inviteZone(), routes.javascript.AddZoneController.inviteZoneForm)
  val ADD_ZONE = new Form("ADD_ZONE", routes.AddZoneController.addZone(), routes.javascript.AddZoneController.addZoneForm)
  val REVIEW_ADD_ZONE_ON_COMPLETION = new Form("REVIEW_ADD_ZONE_ON_COMPLETION", routes.AddZoneController.userReviewAddZoneRequest(), routes.javascript.AddZoneController.userReviewAddZoneRequestForm)
  val VERIFY_ZONE = new Form("VERIFY_ZONE", routes.AddZoneController.verifyZone(), routes.javascript.AddZoneController.verifyZoneForm)
  val REJECT_VERIFY_ZONE_REQUEST = new Form("REJECT_VERIFY_ZONE_REQUEST", routes.AddZoneController.rejectVerifyZoneRequest(), routes.javascript.AddZoneController.rejectVerifyZoneRequestForm)
  val BLOCKCHAIN_ADD_ZONE = new Form("BLOCKCHAIN_ADD_ZONE", routes.AddZoneController.blockchainAddZone(), routes.javascript.AddZoneController.blockchainAddZoneForm)
  val UPDATE_ZONE_KYC_DOCUMENT_STATUS = new Form("UPDATE_ZONE_KYC_DOCUMENT_STATUS", routes.AddZoneController.updateZoneKYCDocumentStatus(), routes.javascript.AddZoneController.updateZoneKYCDocumentStatusForm)

  //BackgroundCheckController
  val MEMBER_CHECK_MEMBER_SCAN = new Form("MEMBER_CHECK_MEMBER_SCAN", routes.BackgroundCheckController.memberScan(), routes.javascript.BackgroundCheckController.memberScanForm)
  val MEMBER_CHECK_MEMBER_SCAN_RESULT_DECISION = new Form("MEMBER_CHECK_MEMBER_SCAN_RESULT_DECISION", routes.BackgroundCheckController.memberScanResultDecision(), routes.javascript.BackgroundCheckController.memberScanResultDecisionForm)
  val ADD_UBO_MEMBER_CHECK = new Form("ADD_UBO_MEMBER_CHECK", routes.BackgroundCheckController.addUBOMemberCheck(), routes.javascript.BackgroundCheckController.addUBOMemberCheckForm)
  val MEMBER_CHECK_CORPORATE_SCAN = new Form("MEMBER_CHECK_CORPORATE_SCAN", routes.BackgroundCheckController.corporateScan(), routes.javascript.BackgroundCheckController.corporateScanForm)
  val MEMBER_CHECK_CORPORATE_SCAN_RESULT_DECISION = new Form("MEMBER_CHECK_CORPORATE_SCAN_RESULT_DECISION", routes.BackgroundCheckController.corporateScanResultDecision(), routes.javascript.BackgroundCheckController.corporateScanResultDecisionForm)
  val ADD_ORGANIZATION_MEMBER_CHECK = new Form("ADD_ORGANIZATION_MEMBER_CHECK", routes.BackgroundCheckController.addOrganizationMemberCheck(), routes.javascript.BackgroundCheckController.addOrganizationMemberCheckForm)
  val MEMBER_CHECK_VESSEL_SCAN = new Form("MEMBER_CHECK_VESSEL_SCAN", routes.BackgroundCheckController.vesselScan(), routes.javascript.BackgroundCheckController.vesselScanForm)
  val MEMBER_CHECK_VESSEL_SCAN_RESULT_DECISION = new Form("MEMBER_CHECK_VESSEL_SCAN_RESULT_DECISION", routes.BackgroundCheckController.vesselScanResultDecision(), routes.javascript.BackgroundCheckController.vesselScanResultDecisionForm)
  val ADD_ASSET_MEMBER_CHECK = new Form("ADD_ASSET_MEMBER_CHECK", routes.BackgroundCheckController.addAssetMemberCheck(), routes.javascript.BackgroundCheckController.addAssetMemberCheckForm)


  //ChangeBuyerBidController
  val BLOCKCHAIN_CHANGE_BUYER_BID = new Form("BLOCKCHAIN_CHANGE_BUYER_BID", routes.ChangeBuyerBidController.blockchainChangeBuyerBid(), routes.javascript.ChangeBuyerBidController.blockchainChangeBuyerBidForm)

  //ChangeSellerBidController
  val BLOCKCHAIN_CHANGE_SELLER_BID = new Form("BLOCKCHAIN_CHANGE_SELLER_BID", routes.ChangeSellerBidController.blockchainChangeSellerBid(), routes.javascript.ChangeSellerBidController.blockchainChangeSellerBidForm)

  //ConfirmBuyerBidController
  val BLOCKCHAIN_CONFIRM_BUYER_BID = new Form("BLOCKCHAIN_CONFIRM_BUYER_BID", routes.ConfirmBuyerBidController.blockchainConfirmBuyerBid(), routes.javascript.ConfirmBuyerBidController.blockchainConfirmBuyerBidForm)

  //ConfirmSellerBidController
  val BLOCKCHAIN_CONFIRM_SELLER_BID = new Form("BLOCKCHAIN_CONFIRM_SELLER_BID", routes.ConfirmSellerBidController.blockchainConfirmSellerBid(), routes.javascript.ConfirmSellerBidController.blockchainConfirmSellerBidForm)

  //ChatController
  val SEND_MESSAGE = new Form("SEND_MESSAGE", routes.ChatController.sendMessage(), routes.javascript.ChatController.sendMessageForm)

  //ContactController
  val ADD_OR_UPDATE_EMAIL_ADDRESS = new Form("ADD_OR_UPDATE_EMAIL_ADDRESS", routes.ContactController.addOrUpdateEmailAddress(), routes.javascript.ContactController.addOrUpdateEmailAddressForm)
  val ADD_OR_UPDATE_MOBILE_NUMBER = new Form("ADD_OR_UPDATE_MOBILE_NUMBER", routes.ContactController.addOrUpdateMobileNumber(), routes.javascript.ContactController.addOrUpdateMobileNumberForm)
  val VERIFY_EMAIL_ADDRESS = new Form("VERIFY_EMAIL_ADDRESS", routes.ContactController.verifyEmailAddress(), routes.javascript.ContactController.verifyEmailAddressForm)
  val VERIFY_MOBILE_NUMBER = new Form("VERIFY_MOBILE_NUMBER", routes.ContactController.verifyMobileNumber(), routes.javascript.ContactController.verifyMobileNumberForm)

  //IssueAssetController
  val ZONE_ISSUE_ASSET = new Form("ZONE_ISSUE_ASSET", routes.IssueAssetController.issueAsset(), routes.javascript.IssueAssetController.issueAssetForm)
  val BLOCKCHAIN_ISSUE_ASSET = new Form("BLOCKCHAIN_ISSUE_ASSET", routes.IssueAssetController.blockchainIssueAsset(), routes.javascript.IssueAssetController.blockchainIssueAssetForm)

  //IssueFiatController
  val ISSUE_FIAT_REQUEST = new Form("ISSUE_FIAT_REQUEST", routes.WesternUnionController.westernUnionPortalRedirect(), routes.javascript.IssueFiatController.issueFiatRequestForm)
  val ISSUE_FIAT = new Form("ISSUE_FIAT", routes.IssueFiatController.issueFiat(), routes.javascript.IssueFiatController.issueFiatForm)
  val BLOCKCHAIN_ISSUE_FIAT = new Form("BLOCKCHAIN_ISSUE_FIAT", routes.IssueFiatController.blockchainIssueFiat(), routes.javascript.IssueFiatController.blockchainIssueFiatForm)

  //NegotiationController
  val NEGOTIATION_REQUEST = new Form("NEGOTIATION_REQUEST", routes.NegotiationController.request(), routes.javascript.NegotiationController.requestForm)
  val NEGOTIATION_PAYMENT_TERMS = new Form("NEGOTIATION_PAYMENT_TERMS", routes.NegotiationController.paymentTerms(), routes.javascript.NegotiationController.paymentTermsForm)
  val NEGOTIATION_DOCUMENT_LIST = new Form("NEGOTIATION_DOCUMENT_LIST", routes.NegotiationController.documentList(), routes.javascript.NegotiationController.documentListForm)
  val REVIEW_NEGOTIATION_REQUEST = new Form("REVIEW_NEGOTIATION_REQUEST", routes.NegotiationController.reviewRequest(), routes.javascript.NegotiationController.reviewRequestForm)
  val ACCEPT_NEGOTIATION_REQUEST = new Form("ACCEPT_NEGOTIATION_REQUEST", routes.NegotiationController.acceptRequest(), routes.javascript.NegotiationController.acceptRequestForm)
  val REJECT_NEGOTIATION_REQUEST = new Form("REJECT_NEGOTIATION_REQUEST", routes.NegotiationController.rejectRequest(), routes.javascript.NegotiationController.rejectRequestForm)
  val UPDATE_ASSET_TERMS = new Form("UPDATE_ASSET_TERMS", routes.NegotiationController.updateAssetTerms(), routes.javascript.NegotiationController.updateAssetTermsForm)
  val UPDATE_ASSET_OTHER_DETAILS = new Form("UPDATE_ASSET_OTHER_DETAILS", routes.NegotiationController.updateAssetOtherDetails(), routes.javascript.NegotiationController.updateAssetOtherDetailsForm)
  val UPDATE_PAYMENT_TERMS = new Form("UPDATE_PAYMENT_TERMS", routes.NegotiationController.updatePaymentTerms(), routes.javascript.NegotiationController.updatePaymentTermsForm)
  val UPDATE_DOCUMENT_LIST = new Form("UPDATE_DOCUMENT_LIST", routes.NegotiationController.updateDocumentList(), routes.javascript.NegotiationController.updateDocumentListForm)
  val ACCEPT_OR_REJECT_NEGOTIATION_TERMS = new Form("ACCEPT_OR_REJECT_NEGOTIATION_TERMS", routes.NegotiationController.acceptOrRejectNegotiationTerms(), routes.javascript.NegotiationController.acceptOrRejectNegotiationTermsForm)
  val ADD_INVOICE = new Form("ADD_INVOICE", routes.NegotiationController.addInvoice(), routes.javascript.NegotiationController.addInvoiceForm)
  val ADD_CONTRACT = new Form("ADD_CONTRACT", routes.NegotiationController.addContract(), routes.javascript.NegotiationController.addContractForm)
  val CONFIRM_ALL_NEGOTIATION_TERMS = new Form("CONFIRM_ALL_NEGOTIATION_TERMS", routes.NegotiationController.confirmAllNegotiationTerms(), routes.javascript.NegotiationController.confirmAllNegotiationTermsForm)
  val BUYER_CONFIRM_NEGOTIATION = new Form("BUYER_CONFIRM_NEGOTIATION", routes.NegotiationController.buyerConfirm(), routes.javascript.NegotiationController.buyerConfirmForm)
  val SELLER_CONFIRM_NEGOTIATION = new Form("SELLER_CONFIRM_NEGOTIATION", routes.NegotiationController.sellerConfirm(), routes.javascript.NegotiationController.sellerConfirmForm)
  val UPDATE_CONTRACT_SIGNED = new Form("UPDATE_CONTRACT_SIGNED", routes.NegotiationController.updateContractSigned(), routes.javascript.NegotiationController.updateContractSignedForm)

  //OrderController
  val MODERATED_BUYER_EXECUTE_ORDER = new Form("MODERATED_BUYER_EXECUTE_ORDER", routes.OrderController.moderatedBuyerExecute(), routes.javascript.OrderController.moderatedBuyerExecuteForm)
  val MODERATED_SELLER_EXECUTE_ORDER = new Form("MODERATED_SELLER_EXECUTE_ORDER", routes.OrderController.moderatedSellerExecute(), routes.javascript.OrderController.moderatedSellerExecuteForm)
  val BUYER_EXECUTE_ORDER = new Form("BUYER_EXECUTE_ORDER", routes.OrderController.buyerExecute(), routes.javascript.OrderController.buyerExecuteForm)
  val SELLER_EXECUTE_ORDER = new Form("SELLER_EXECUTE_ORDER", routes.OrderController.sellerExecute(), routes.javascript.OrderController.sellerExecuteForm)
  val BLOCKCHAIN_BUYER_EXECUTE_ORDER = new Form("BLOCKCHAIN_BUYER_EXECUTE_ORDER", routes.OrderController.blockchainBuyerExecute(), routes.javascript.OrderController.blockchainBuyerExecuteForm)
  val BLOCKCHAIN_SELLER_EXECUTE_ORDER = new Form("BLOCKCHAIN_SELLER_EXECUTE_ORDER", routes.OrderController.blockchainSellerExecute(), routes.javascript.OrderController.blockchainSellerExecuteForm)

  //RedeemAssetController
  val BLOCKCHAIN_REDEEM_ASSET = new Form("BLOCKCHAIN_REDEEM_ASSET", routes.RedeemAssetController.blockchainRedeemAsset(), routes.javascript.RedeemAssetController.blockchainRedeemAssetForm)

  //RedeemFiatController
  val REDEEM_FIAT = new Form("REDEEM_FIAT", routes.RedeemFiatController.redeemFiat(), routes.javascript.RedeemFiatController.redeemFiatForm)
  val BLOCKCHAIN_REDEEM_FIAT = new Form("BLOCKCHAIN_REDEEM_FIAT", routes.RedeemFiatController.blockchainRedeemFiat(), routes.javascript.RedeemFiatController.blockchainRedeemFiatForm)
  val ZONE_REDEEM_FIAT = new Form("ZONE_REDEEM_FIAT", routes.RedeemFiatController.zoneRedeemFiat(), routes.javascript.RedeemFiatController.zoneRedeemFiatForm)

  //ReleaseAssetController
  val BLOCKCHAIN_RELEASE_ASSET = new Form("BLOCKCHAIN_RELEASE_ASSET", routes.ReleaseAssetController.blockchainReleaseAsset(), routes.javascript.ReleaseAssetController.blockchainReleaseAssetForm)

  //SendAssetController
  val BLOCKCHAIN_SEND_ASSET = new Form("BLOCKCHAIN_SEND_ASSET", routes.SendAssetController.blockchainSendAsset(), routes.javascript.SendAssetController.blockchainSendAssetForm)

  //SendCoinController
  val SEND_COIN = new Form("SEND_COIN", routes.SendCoinController.sendCoin(), routes.javascript.SendCoinController.sendCoinForm)
  val FAUCET_REQUEST = new Form("FAUCET_REQUEST", routes.SendCoinController.faucetRequest(), routes.javascript.SendCoinController.faucetRequestForm)
  val REJECT_FAUCET_REQUEST = new Form("REJECT_FAUCET_REQUEST", routes.SendCoinController.rejectFaucetRequest(), routes.javascript.SendCoinController.rejectFaucetRequestForm)
  val APPROVE_FAUCET_REQUEST = new Form("APPROVE_FAUCET_REQUEST", routes.SendCoinController.approveFaucetRequests(), routes.javascript.SendCoinController.approveFaucetRequestsForm)
  val BLOCKCHAIN_SEND_COIN = new Form("BLOCKCHAIN_SEND_COIN", routes.SendCoinController.blockchainSendCoin(), routes.javascript.SendCoinController.blockchainSendCoinForm)

  //SendFiatController
  val SEND_FIAT = new Form("SEND_FIAT", routes.SendFiatController.sendFiat(), routes.javascript.SendFiatController.sendFiatForm)
  val BLOCKCHAIN_SEND_FIAT = new Form("BLOCKCHAIN_SEND_FIAT", routes.SendFiatController.blockchainSendFiat(), routes.javascript.SendFiatController.blockchainSendFiatForm)
  val ZONE_SEND_FIAT = new Form("ZONE_SEND_FIAT", routes.SendFiatController.zoneSendFiat(), routes.javascript.SendFiatController.zoneSendFiatForm)

  //SetACLController
  val INVITE_TRADER = new Form("INVITE_TRADER", routes.SetACLController.inviteTrader(), routes.javascript.SetACLController.inviteTraderForm)
  val ADD_TRADER = new Form("ADD_TRADER", routes.SetACLController.addTrader(), routes.javascript.SetACLController.addTraderForm)
  val ZONE_VERIFY_TRADER = new Form("ZONE_VERIFY_TRADER", routes.SetACLController.zoneVerifyTrader(), routes.javascript.SetACLController.zoneVerifyTraderForm)
  val ORGANIZATION_VERIFY_TRADER = new Form("ORGANIZATION_VERIFY_TRADER", routes.SetACLController.organizationVerifyTrader(), routes.javascript.SetACLController.organizationVerifyTraderForm)
  val BLOCKCHAIN_SET_ACL = new Form("BLOCKCHAIN_SET_ACL", routes.SetACLController.blockchainSetACL(), routes.javascript.SetACLController.blockchainSetACLForm)

  //SetBuyerFeedbackController
  val SET_BUYER_FEEDBACK = new Form("SET_BUYER_FEEDBACK", routes.SetBuyerFeedbackController.setBuyerFeedback(), routes.javascript.SetBuyerFeedbackController.setBuyerFeedbackForm)
  val BLOCKCHAIN_SET_BUYER_FEEDBACK = new Form("BLOCKCHAIN_SET_BUYER_FEEDBACK", routes.SetBuyerFeedbackController.blockchainSetBuyerFeedback(), routes.javascript.SetBuyerFeedbackController.blockchainSetBuyerFeedbackForm)

  //SetSellerFeedbackController
  val SET_SELLER_FEEDBACK = new Form("SET_SELLER_FEEDBACK", routes.SetSellerFeedbackController.setSellerFeedback(), routes.javascript.SetSellerFeedbackController.setSellerFeedbackForm)
  val BLOCKCHAIN_SET_SELLER_FEEDBACK = new Form("BLOCKCHAIN_SET_SELLER_FEEDBACK", routes.SetSellerFeedbackController.blockchainSetSellerFeedback(), routes.javascript.SetSellerFeedbackController.blockchainSetSellerFeedbackForm)

  //TraderController
  val ORGANIZATION_REJECT_TRADER_REQUEST = new Form("ORGANIZATION_REJECT_TRADER_REQUEST", routes.TraderController.organizationRejectRequest(), routes.javascript.TraderController.organizationRejectRequestForm)
  val ZONE_REJECT_TRADER_REQUEST = new Form("ZONE_REJECT_TRADER_REQUEST", routes.TraderController.zoneRejectRequest(), routes.javascript.TraderController.zoneRejectRequestForm)
  val TRADER_RELATION_REQUEST = new Form("TRADER_RELATION_REQUEST", routes.TraderController.traderRelationRequest(), routes.javascript.TraderController.traderRelationRequestForm)
  val MODIFY_TRADER = new Form("MODIFY_TRADER", routes.TraderController.organizationModifyTrader(), routes.javascript.TraderController.organizationModifyTraderForm)
  val ACCEPT_REJECT_TRADER_RELATION = new Form("ACCEPT_REJECT_TRADER_RELATION", routes.TraderController.acceptOrRejectTraderRelation(), routes.javascript.TraderController.acceptOrRejectTraderRelationForm)

  //WALLEX CONTROLLER
  val ADD_OR_UPDATE_WALLEX_ACCOUNT = new Form("ADD_OR_UPDATE_WALLEX_ACCOUNT", routes.WallexController.createOrganizationAccount(), routes.javascript.WallexController.createOrganizationAccountForm)
  val UPDATE_WALLEX_ACCOUNT = new Form("UPDATE_WALLEX_ACCOUNT", routes.WallexController.updateCompanyAccount(), routes.javascript.WallexController.updateCompanyAccountForm)
  val ADD_OR_UPDATE_WALLEX_DOCUMENT = new Form("ADD_OR_UPDATE_WALLEX_DOCUMENT", routes.WallexController.wallexDocument(), routes.javascript.WallexController.wallexDocumentForm)
  val INITIATE_WALLEX_PAYMENT = new Form("INITIATE_WALLEX_PAYMENT", routes.WallexController.initiatePayment(), routes.javascript.WallexController.initiatePaymentForm)
  val ACCEPT_WALLEX_PAYMENT_QUOTE_REQUEST = new Form("ACCEPT_WALLEX_PAYMENT_QUOTE_REQUEST", routes.WallexController.acceptWallexQuote(), routes.javascript.WallexController.acceptWallexQuoteForm)
  val ADD_BENEFICIARY_DETAILS_WALLEX = new Form("ADD_BENEFICIARY_DETAILS_WALLEX", routes.WallexController.createBeneficiaries(), routes.javascript.WallexController.createBeneficiariesForm)
  val DELETE_WALLEX_BENEFICIARY = new Form("DELETE_WALLEX_BENEFICIARY", routes.WallexController.deleteBeneficiary(), routes.javascript.WallexController.deleteBeneficiaryForm)
  val WALLEX_WALLET_TRANSFER = new Form("WALLEX_WALLET_TRANSFER", routes.WallexController.walletTransfer(), routes.javascript.WallexController.walletTransferForm)
  val ZONE_WALLEX_WALLET_TRANSFER = new Form("ZONE_WALLEX_WALLET_TRANSFER", routes.WallexZoneController.zoneCreateWalletTransfer(), routes.javascript.WallexZoneController.zoneWalletTransferForm)
  val WALLEX_CREATE_COLLECTION_ACCOUNT = new Form("WALLEX_CREATE_COLLECTION_ACCOUNT", routes.WallexController.createCollectionAccounts(), routes.javascript.WallexController.createCollectionAccountsForm)
  val GET_COLLECTION_ACCOUNT_DETAILS = new Form("GET_COLLECTION_ACCOUNT_DETAILS", routes.WallexController.getCollectionAccounts(), routes.javascript.WallexController.getCollectionAccountsForm)
  val WALLEX_SUBMIT_DOCUMENT = new Form("WALLEX_SUBMIT_DOCUMENT", routes.WallexController.submitDocumentToWallex(), routes.javascript.WallexController.submitDocumentToWallexForm)
  val WALLEX_GET_USER = new Form("WALLEX_GET_USER", routes.WallexController.getUser(), routes.javascript.WallexController.getUserForm)
  val WALLEX_SEND_USER_FOR_SCREENING = new Form("WALLEX_SEND_USER_FOR_SCREENING", routes.WallexZoneController.sendForScreening(), routes.javascript.WallexZoneController.sendForScreeningForm)
  val UPDATE_USER_DETAILS = new Form("UPDATE_USER_DETAILS", routes.WallexController.updateAccount(), routes.javascript.WallexController.updateAccountForm)

  //Western Union - Please Do not change.
  val WU_RTCB_ID = "id"
  val REFERENCE = "reference"
  val EXTERNAL_REFERENCE = "externalReference"
  val INVOICE_NUMBER = "invoiceNumber"
  val BUYER_BUSINESS_ID = "buyerBusinessId"
  val BUYER_FIRST_NAME = "buyerFirstName"
  val BUYER_LAST_NAME = "buyerLastName"
  val CREATED_DATE = "createdDate"
  val LAST_UPDATED_DATE = "lastUpdatedDate"
  val WU_RTCB_STATUS = "status"
  val DEAL_TYPE = "dealType"
  val PAYMENT_TYPE_ID = "paymentTypeId"
  val PAID_OUT_AMOUNT = "paidOutAmount"
  val REQUEST_SIGNATURE = "requestSignature"
  val CLIENT_ID = "clientId"
  val CLIENT_REFERENCE = "clientReference"
  val WU_SFTP_BUYER_ID = "buyer.id"
  val WU_SFTP_BUYER_FIRST_NAME = "buyer.firstName"
  val WU_SFTP_BUYER_LAST_NAME = "buyer.lastName"
  val WU_SFTP_BUYER_ADDRESS = "buyer.address"
  val BUYER_CITY = "buyer.city"
  val BUYER_ZIP = "buyer.zip"
  val BUYER_EMAIL = "buyer.email"
  val SERVICE_ID = "service.id"
  val SERVICE_AMOUNT = "service.amount"

  //Gatling Test
  val ADDRESS_ADDRESS_LINE_1 = "ADDRESS.ADDRESS_LINE_1"
  val ADDRESS_ADDRESS_LINE_2 = "ADDRESS.ADDRESS_LINE_2"
  val ADDRESS_LANDMARK = "ADDRESS.LANDMARK"
  val ADDRESS_CITY = "ADDRESS.CITY"
  val ADDRESS_COUNTRY = "ADDRESS.COUNTRY"
  val ADDRESS_ZIP_CODE = "ADDRESS.ZIP_CODE"
  val ADDRESS_PHONE = "ADDRESS.PHONE"
  val COMPLETION = "COMPLETION"
  val MODE = "MODE"

  val RESUMABLE_CHUNK_NUMBER = "resumableChunkNumber"
  val RESUMABLE_CHUNK_SIZE = "resumableChunkSize"
  val RESUMABLE_TOTAL_SIZE = "resumableTotalSize"
  val RESUMABLE_IDENTIFIER = "resumableIdentifier"
  val RESUMABLE_FILE_NAME = "resumableFilename"

}
