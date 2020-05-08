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
  val CREATE_BLOCKCHAIN_ACCOUNT = new Form("CREATE_BLOCKCHAIN_ACCOUNT", routes.AccountController.createBlockchain(), routes.javascript.AccountController.createBlockchainForm)
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
  val USER_ADD_OR_UPDATE_UBOS = new Form("USER_ADD_OR_UPDATE_UBOS", routes.AddOrganizationController.userAddOrUpdateUBOs(), routes.javascript.AddOrganizationController.userAddOrUpdateUBOsForm)
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

  val ACCEPT_OFFER = "ACCEPT_OFFER"
  val ASSETS = "ASSETS"
  val BLOCKS = "BLOCKS"
  val BLOCK = "BLOCK"
  val BUY = "BUY"
  val NEXT = "NEXT"
  val ADD = "ADD"
  val BLOCK_TIMES = "BLOCK_TIMES"
  val SEARCH = "SEARCH"
  val PREVIOUS = "PREVIOUS"
  val ORGANIZATION_VERIFICATION = "ORGANIZATION_VERIFICATION"
  val ORGANIZATION_BACKGROUND_CHECK = "ORGANIZATION_BACKGROUND_CHECK"
  val ZONE_VERIFICATION = "ZONE_VERIFICATION"
  val EVIDENCE_HASH = "EVIDENCE_HASH"
  val SEE_ALL = "SEE_ALL"
  val ACL = "ACL"
  val LATEST_BLOCK_HEIGHT = "LATEST_BLOCK_HEIGHT"
  val AVERAGE_BLOCK_TIME = "AVERAGE_BLOCK_TIME"
  val INVALID_BLOCK_HEIGHT = "INVALID_BLOCK_HEIGHT"
  val INVALID_TRANSACTION_HASH = "INVALID_TRANSACTION_HASH"
  val LOAD_MORE_NOTIFICATIONS = "LOAD_MORE_NOTIFICATIONS"
  val ZONES = "ZONES"
  val INBOX = "INBOX"
  val NEGOTIATE = "NEGOTIATE"
  val ON_GOING_NEGOTIATIONS = "ON_GOING_NEGOTIATIONS"
  val ON_GOING_ORDERS = "ON_GOING_ORDERS"
  val SHOW_HIDE = "SHOW_HIDE"
  val SUBMIT_FEEDBACK = "SUBMIT_FEEDBACK"
  val BUY_ORDER_FEEDBACKS = "BUY_ORDER_FEEDBACKS"
  val SELL_ORDER_FEEDBACKS = "SELL_ORDER_FEEDBACKS"
  val SELL = "SELL"
  val PROFILE_PICTURE = "PROFILE_PICTURE"
  val GET_OTP = "GET_OTP"
  val SET_PASSWORD = "SET_PASSWORD"
  val REVIEW = "REVIEW"
  val SUBMIT = "SUBMIT"
  val ORGANIZATION_DETAILS = "ORGANIZATION_DETAILS"
  val ORGANIZATION_KYC_FILES = "ORGANIZATION_KYC_FILES"
  val ORGANIZATION_BACKGROUND_CHECK_FILES = "ORGANIZATION_BACKGROUND_CHECK_FILES"
  val DELETE = "DELETE"
  val ZONE_DETAILS = "ZONE_DETAILS"
  val TRADER_KYC_FILES = "TRADER_KYC_FILES"
  val TRADER_BACKGROUND_CHECK = "TRADER_BACKGROUND_CHECK"
  val TRADER_DETAILS = "TRADER_DETAILS"
  val USER_DETAILS = "USER_DETAILS"
  val RATING = "RATING"
  val TOTAL_ASSETS_OWNED = "TOTAL_ASSETS_OWNED"
  val TOTAL_FIATS = "TOTAL_FIATS"
  val ON_GOING_BUY_NEGOTIATIONS = "ON_GOING_BUY_NEGOTIATIONS"
  val ON_GOING_SELL_NEGOTIATIONS = "ON_GOING_SELL_NEGOTIATIONS"
  val ON_GOING_BUY_ORDERS = "ON_GOING_BUY_ORDERS"
  val ON_GOING_SELL_ORDERS = "ON_GOING_SELL_ORDERS"
  val VIEW = "VIEW"
  val TEAM = "TEAM"
  val VERIFY = "VERIFY"
  val ZONE_KYC_FILES = "ZONE_KYC_FILES"
  val UPDATE_ZONE_KYC_STATUS = "UPDATE_ZONE_KYC_STATUS"
  val UPDATE_ORGANIZATION_KYC_STATUS = "UPDATE_ORGANIZATION_KYC_STATUS"
  val UPDATE_STATUS = "UPDATE_STATUS"
  val STATUS_UPDATED = "STATUS_UPDATED"
  val UNAUTHORIZED_TRANSACTION = "UNAUTHORIZED_TRANSACTION"
  val HOME = "HOME"
  val COINS = "COINS"
  val ASSETS_OWNED = "ASSETS_OWNED"
  val FIATS_OWNED = "FIATS_OWNED"
  val FIATS_TOTAL_VALUE = "FIATS_TOTAL_VALUE"
  val ID_TYPE = "ID_TYPE"
  val ID_NUMBER = "ID_NUMBER"
  val FIRST_NAME = "FIRST_NAME"
  val LAST_NAME = "LAST_NAME"
  val DATE_OF_BIRTH = "DATE_OF_BIRTH"
  val CREATE_SALES_QUOTE = "CREATE_SALES_QUOTE"

  //File Upload
  val BROWSE = "BROWSE"
  val OR = "OR"
  val DROP_FILE = "DROP_FILE"
  val UPLOAD = "UPLOAD"
  val UPDATE = "UPDATE"
  val DOCUMENTS = "DOCUMENTS"
  val CONTRACT = "CONTRACT"
  val INVOICE = "INVOICE"
  val PACKING_LIST = "PACKING_LIST"
  val COO = "COO"
  val COA = "COA"

  val UPLOAD_BANK_DETAILS = "UPLOAD_BANK_DETAILS"
  val UPLOAD_IDENTIFICATION = "UPLOAD_IDENTIFICATION"
  val UPDATE_BANK_ACCOUNT_DETAIL = "UPDATE_BANK_ACCOUNT_DETAIL"
  val UPDATE_IDENTIFICATION = "UPDATE_IDENTIFICATION"
  val UPLOAD_ZONE_BANK_ACCOUNT_DETAIL = "UPLOAD_ZONE_BANK_ACCOUNT_DETAIL"
  val UPLOAD_ZONE_IDENTIFICATION = "UPLOAD_ZONE_IDENTIFICATION"
  val UPLOAD_ORGANIZATION_BANK_ACCOUNT_DETAIL = "UPLOAD_ORGANIZATION_BANK_ACCOUNT_DETAIL"
  val UPLOAD_ORGANIZATION_IDENTIFICATION = "UPLOAD_ORGANIZATION_IDENTIFICATION"
  val UPLOAD_LATEST_AUDITED_FINANCIAL_REPORT = "UPLOAD_LATEST_AUDITED_FINANCIAL_REPORT"
  val UPDATE_LATEST_AUDITED_FINANCIAL_REPORT = "UPDATE_LATEST_AUDITED_FINANCIAL_REPORT"
  val UPLOAD_LAST_YEAR_AUDITED_FINANCIAL_REPORT = "UPLOAD_LAST_YEAR_AUDITED_FINANCIAL_REPORT"
  val UPDATE_LAST_YEAR_AUDITED_FINANCIAL_REPORT = "UPDATE_LAST_YEAR_AUDITED_FINANCIAL_REPORT"
  val UPLOAD_MANAGEMENT = "UPLOAD_MANAGEMENT"
  val UPDATE_MANAGEMENT = "UPDATE_MANAGEMENT"
  val UPLOAD_ACRA = "UPLOAD_ACRA"
  val UPDATE_ACRA = "UPDATE_ACRA"
  val UPLOAD_SHARE_STRUCTURE = "UPLOAD_SHARE_STRUCTURE"
  val UPDATE_SHARE_STRUCTURE = "UPDATE_SHARE_STRUCTURE"
  val UPLOAD_BANK_ACCOUNT_DETAIL = "UPLOAD_BANK_ACCOUNT_DETAIL"
  val UPLOAD_TRADER_IDENTIFICATION = "UPLOAD_TRADER_IDENTIFICATION"
  val UPDATE_TRADER_IDENTIFICATION = "UPDATE_TRADER_IDENTIFICATION"
  val UPDATE_ZONE_BANK_ACCOUNT_DETAIL = "UPDATE_ZONE_BANK_ACCOUNT_DETAIL"
  val UPDATE_ZONE_IDENTIFICATION = "UPDATE_ZONE_IDENTIFICATION"
  val UPLOAD_ADMIN_PROFILE_IDENTIFICATION = "UPLOAD_ADMIN_PROFILE_IDENTIFICATION"
  val UPDATE_ADMIN_PROFILE_IDENTIFICATION = "UPDATE_ADMIN_PROFILE_IDENTIFICATION"

  val UPDATE_ORGANIZATION_BANK_ACCOUNT_DETAIL = "UPDATE_ORGANIZATION_BANK_ACCOUNT_DETAIL"
  val UPDATE_ORGANIZATION_IDENTIFICATION = "UPDATE_ORGANIZATION_IDENTIFICATION"
  val UPLOAD_INVOICE_DOCUMENT = "UPLOAD_INVOICE_DOCUMENT"
  val UPDATE_INVOICE_DOCUMENT = "UPDATE_INVOICE_DOCUMENT"
  val UPLOAD_CONTRACT_DOCUMENT = "UPLOAD_CONTRACT_DOCUMENT"
  val UPDATE_CONTRACT_DOCUMENT = "UPDATE_CONTRACT_DOCUMENT"
  val UPLOAD_PACKING_LIST_DOCUMENT = "UPLOAD_PACKING_LIST_DOCUMENT"
  val UPDATE_PACKING_LIST_DOCUMENT = "UPDATE_PACKING_LIST_DOCUMENT"
  val UPLOAD_COO_DOCUMENT = "UPLOAD_COO_DOCUMENT"
  val UPDATE_COO_DOCUMENT = "UPDATE_COO_DOCUMENT"
  val UPLOAD_COA_DOCUMENT = "UPLOAD_COA_DOCUMENT"
  val UPDATE_COA_DOCUMENT = "UPDATE_COA_DOCUMENT"
  val UPLOAD_OTHER_DOCUMENT = "UPLOAD_OTHER_DOCUMENT"
  val UPDATE_OTHER_DOCUMENT = "UPDATE_OTHER_DOCUMENT"
  val UPLOAD_BUYER_CONTRACT = "UPLOAD_BUYER_CONTRACT"
  val UPDATE_BUYER_CONTRACT = "UPDATE_BUYER_CONTRACT"
  val UPLOAD_SELLER_CONTRACT = "UPLOAD_SELLER_CONTRACT"
  val UPDATE_SELLER_CONTRACT = "UPDATE_SELLER_CONTRACT"

  val HEIGHT = "HEIGHT"
  val FEE = "FEE"
  val TRANSACTION_HASH = "TRANSACTION_HASH"
  val TYPE = "TYPE"
  val NUM_TXS = "NUM_TXS"
  val TRANSACTIONS = "TRANSACTIONS"
  val DATA_HASH = "DATA_HASH"
  val VALIDATORS = "VALIDATORS"
  val VALIDATORS_HASH = "VALIDATORS_HASH"
  val OPERATOR = "OPERATOR"
  val TOKENS = "TOKENS"
  val DELEGATOR_SHARES = "DELEGATOR_SHARES"

  //Blockchain
  val TAKER_ADDRESS = "TAKER_ADDRESS"
  val BLOCKCHAIN_ADDRESS = "BLOCKCHAIN_ADDRESS"
  val PUBLIC_KEY = "PUBLIC_KEY"
  val MNEMONIC = "MNEMONIC"
  val PASSWORD = "PASSWORD"
  val CONFIRM_PASSWORD = "CONFIRM_PASSWORD"
  val FROM = "FROM"
  val TO = "TO"
  val ZONE = "ZONE"
  val GENESIS = "GENESIS"
  val GENESIS_ADDRESS = "GENESIS_ADDRESS"
  val ORGANIZATION_NAME = "ORGANIZATION_NAME"
  val ORGANIZATION_USERNAME = "ORGANIZATION_USERNAME"
  val ZONE_ID = "ZONE_ID"
  val NAME = "NAME"
  val BUYER_ADDRESS = "BUYER_ADDRESS"
  val BUYER_ID = "BUYER_ID"
  val SELLER_ADDRESS = "SELLER_ADDRESS"
  val SELLER_ID = "SELLER_ID"
  val OWNER_ADDRESS = "OWNER_ADDRESS"
  val FIAT_PROOF_HASH = "FIAT_PROOF_HASH"
  val PEG_HASH = "PEG_HASH"
  val GAS = "GAS"
  val BID = "BID"
  val TIME = "TIME"
  val BUYER_CONTRACT_HASH = "BUYER_CONTRACT_HASH"
  val SELLER_CONTRACT_HASH = "SELLER_CONTRACT_HASH"
  val BUYER_BLOCK_HEIGHT = "BUYER_BLOCK_HEIGHT"
  val SELLER_BLOCK_HEIGHT = "SELLER_BLOCK_HEIGHT"
  val DOCUMENT_HASH = "DOCUMENT_HASH"
  val ASSET_TYPE = "ASSET_TYPE"
  val LOCKED = "LOCKED"
  val ASSET_PRICE = "ASSET_PRICE"
  val QUANTITY_UNIT = "QUANTITY_UNIT"
  val ASSET_QUANTITY = "ASSET_QUANTITY"
  val MODERATED = "MODERATED"
  val TRANSACTION_ID = "TRANSACTION_ID"
  val TRANSACTION_AMOUNT = "TRANSACTION_AMOUNT"
  val REDEEM_AMOUNT = "REDEEM_AMOUNT"
  val AWB_PROOF_HASH = "AWB_PROOF_HASH"
  val AMOUNT = "AMOUNT"
  val ACL_ADDRESS = "ACL_ADDRESS"
  val NEGOTIATION = "NEGOTIATION"
  val NEGOTIATION_ID = "NEGOTIATION_ID"
  val BUYER_SIGNATURE = "BUYER_SIGNATURE"
  val SELLER_SIGNATURE = "SELLER_SIGNATURE"

  //Master
  val ADDRESS = "ADDRESS"
  val PHONE = "PHONE"
  val EMAIL = "EMAIL"
  val ID = "ID"
  val CURRENCY = "CURRENCY"
  val USERNAME = "USERNAME"
  val USERNAME_AVAILABLE = "USERNAME_AVAILABLE"
  val PUSH_NOTIFICATION_TOKEN = "PUSH_NOTIFICATION_TOKEN"
  val CSRF_TOKEN = "csrfToken"
  val MOBILE_NUMBER = "MOBILE_NUMBER"
  val EMAIL_ADDRESS = "EMAIL_ADDRESS"
  val OTP = "OTP"
  val VERIFIED_STATUS = "VERIFIED_STATUS"
  val COMPLETION_STATUS = "COMPLETION_STATUS"
  val LOADING = "LOADING"
  val CONNECTION_ERROR = "CONNECTION_ERROR"
  val FAILURE = "FAILURE"
  val WARNING = "WARNING"
  val SUCCESS = "SUCCESS"
  val INFORMATION = "INFORMATION"
  val ABBREVIATION = "ABBREVIATION"
  val ESTABLISHMENT_DATE = "ESTABLISHMENT_DATE"
  val ACCOUNT_HOLDER_NAME = "ACCOUNT_HOLDER_NAME"
  val NICK_NAME = "NICK_NAME"
  val ACCOUNT_NUMBER = "ACCOUNT_NUMBER"
  val BANK_NAME = "BANK_NAME"
  val SWIFT_CODE = "SWIFT_CODE"
  val BANK_ADDRESS = "BANK_ADDRESS"
  val FULL_ADDRESS = "FULL_ADDRESS"

  //MasterTransaction
  val COUPON = "COUPON"
  val REQUEST_ID = "REQUEST_ID"
  val APPROVE = "APPROVE"
  val REJECT = "REJECT"
  val SHIPMENT_DETAILS = "SHIPMENT_DETAILS"
  val COMDEX = "COMDEX"
  val ONLY_SUPPLIER = "ONLY_SUPPLIER"
  val ONLY_BUYER = "ONLY_BUYER"
  val BOTH_PARTIES = "BOTH_PARTIES"
  val SHIPPING_PERIOD = "SHIPPING_PERIOD"

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

  val CITY = "CITY"
  val COUNTRY = "COUNTRY"
  val ZIP_CODE = "ZIP_CODE"
  val REGISTERED_ADDRESS_LINE_1 = "REGISTERED_ADDRESS.ADDRESS_LINE_1"
  val REGISTERED_ADDRESS_LINE_2 = "REGISTERED_ADDRESS.ADDRESS_LINE_2"
  val REGISTERED_LANDMARK = "REGISTERED_ADDRESS.LANDMARK"
  val REGISTERED_CITY = "REGISTERED_ADDRESS.CITY"
  val REGISTERED_COUNTRY = "REGISTERED_ADDRESS.COUNTRY"
  val REGISTERED_ZIP_CODE = "REGISTERED_ADDRESS.ZIP_CODE"
  val REGISTERED_PHONE = "REGISTERED_ADDRESS.PHONE"

  val POSTAL_ADDRESS_LINE_1 = "POSTAL_ADDRESS.ADDRESS_LINE_1"
  val POSTAL_ADDRESS_LINE_2 = "POSTAL_ADDRESS.ADDRESS_LINE_2"
  val POSTAL_LANDMARK = "POSTAL_ADDRESS.LANDMARK"
  val POSTAL_CITY = "POSTAL_ADDRESS.CITY"
  val POSTAL_COUNTRY = "POSTAL_ADDRESS.COUNTRY"
  val POSTAL_ZIP_CODE = "POSTAL_ADDRESS.ZIP_CODE"
  val POSTAL_PHONE = "POSTAL_ADDRESS.PHONE"

  val STREET_ADDRESS = "STREET_ADDRESS"
  val DOCUMENT_TYPE = "DOCUMENT_TYPE"
  val STATUS = "STATUS"
  val ISSUE_ASSET_ACL = "ISSUE_ASSET"
  val ISSUE_FIAT_ACL = "ISSUE_FIAT"
  val SEND_ASSET_ACL = "SEND_ASSET"
  val SEND_FIAT_ACL = "SEND_FIAT"
  val REDEEM_ASSET_ACL = "REDEEM_ASSET"
  val REDEEM_FIAT_ACL = "REDEEM_FIAT"
  val SELLER_EXECUTE_ORDER_ACL = "SELLER_EXECUTE_ORDER"
  val BUYER_EXECUTE_ORDER_ACL = "BUYER_EXECUTE_ORDER"
  val CHANGE_BUYER_BID_ACL = "CHANGE_BUYER_BID"
  val CHANGE_SELLER_BID_ACL = "CHANGE_SELLER_BID"
  val CONFIRM_BUYER_BID_ACL = "CONFIRM_BUYER_BID"
  val CONFIRM_SELLER_BID_ACL = "CONFIRM_SELLER_BID"
  val NEGOTIATION_ACL = "NEGOTIATION"
  val RELEASE_ASSET_ACL = "RELEASE_ASSET"

  val COMMODITY_NAME = "COMMODITY_NAME"
  val QUALITY = "QUALITY"
  val DELIVERY_TERM = "DELIVERY_TERM"
  val TRADE_TYPE = "TRADE_TYPE"
  val PORT_OF_LOADING = "PORT_OF_LOADING"
  val SHIPPER_NAME = "SHIPPER_NAME"
  val SHIPPER_ADDRESS = "SHIPPER_ADDRESS"
  val NOTIFY_PARTY_NAME = "NOTIFY_PARTY_NAME"
  val NOTIFY_PARTY_ADDRESS = "NOTIFY_PARTY_ADDRESS"
  val PORT_OF_DISCHARGE = "PORT_OF_DISCHARGE"
  val SHIPMENT_DATE = "SHIPMENT_DATE"
  val PHYSICAL_DOCUMENTS_HANDLED_VIA = "PHYSICAL_DOCUMENTS_HANDLED_VIA"
  val COMDEX_PAYMENT_TERMS = "COMDEX_PAYMENT_TERMS"
  val FILE_ID = "FILE_ID"

  val RESUMABLE_CHUNK_NUMBER = "resumableChunkNumber"
  val RESUMABLE_CHUNK_SIZE = "resumableChunkSize"
  val RESUMABLE_TOTAL_SIZE = "resumableTotalSize"
  val RESUMABLE_IDENTIFIER = "resumableIdentifier"
  val RESUMABLE_FILE_NAME = "resumableFilename"

  val CONFIRM_NOTE_NEW_KEY_DETAILS = "CONFIRM_NOTE_NEW_KEY_DETAILS"
  val RECEIVE_NOTIFICATIONS = "RECEIVE_NOTIFICATIONS"

  val BILL_OF_LADING_NUMBER = "BILL_OF_LADING_NUMBER"
  val COUNTRY_CODE = "COUNTRY_CODE"

  //Html Elements
  val PLEASE_BACKUP_THIS_MNEMONIC = "PLEASE_BACKUP_THIS_MNEMONIC"
  val INVALID_INPUT = "INVALID_INPUT"
  val MNEMONIC_NOTED = "MNEMONIC_NOTED"
  val EDIT = "EDIT"
  val TABLE = "TABLE"
  val CARD = "CARD"
  val FOOTER_LOGO = "FOOTER_LOGO"
  val ASSET_DETAILS = "ASSET_DETAILS"

}
